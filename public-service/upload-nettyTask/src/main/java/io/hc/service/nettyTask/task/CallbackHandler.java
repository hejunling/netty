package io.hc.service.nettyTask.task;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.hc.service.nettyTask.cfg.TaskProperties;
import io.hc.service.nettyTask.constant.BussinessConstant;
import io.hc.service.nettyTask.constant.MsgConstant;
import io.hc.service.nettyTask.dao.TaskDao;
import io.hc.service.nettyTask.domain.request.ReqBody;
import io.hc.service.nettyTask.domain.request.ReqCommon;
import io.hc.service.nettyTask.domain.response.RespJson;
import io.hc.service.nettyTask.entity.Task;
import io.hc.service.nettyTask.utils.HmacSha1Util;
import io.hc.service.nettyTask.utils.NoticeUtil;
import io.hc.service.nettyTask.utils.TaskUtil;
import io.hc.service.nettyTask.utils.task.HandleTask;
import io.hc.service.nettyTask.utils.task.TaskBus;

/**
 * 将文件上传到云的结果通知客户端
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component
@EnableConfigurationProperties({ TaskProperties.class })
public class CallbackHandler {

	private static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);

	private final Gson gson = new Gson();

	/** 默认编码方式 */
	private static final String CHARSETNAME_DEFAULT = "UTF-8";

	/** http头部 */
	//private Map<String, Object> header = new HashMap<String, Object>();

	/** 任务持久化类Dao */
	private final TaskDao taskDao;

	/** http请求组件 */
	private final RestTemplate restTemplate;

	/** 通知组件 */
	private final NoticeUtil noticeUtil;

	/** 默认重试次数 */
	private final int defaultRetryTimes;

	@Autowired
	public CallbackHandler(TaskBus taskBus, TaskDao taskDao, RestTemplate restTemplate, NoticeUtil noticeUtil,
			TaskProperties properties) {
		taskBus.register(this);
		this.taskDao = taskDao;
		this.restTemplate = restTemplate;
		this.noticeUtil = noticeUtil;
		this.defaultRetryTimes = properties.getRetryTimes();
	}

	/**
	 * 将上传结果通知回调方
	 *
	 * @param task
	 *            任务内容
	 */
	@HandleTask(taskHandler = BussinessConstant.CALLBACK, description = "将上传结果通知回调方")
	public void doCallback(Task task) {

		boolean retryFlg = false;
		// 取得执行参数
		Map<String, Object> taskParams = task.getParamsMap();
		// 重试原因
		String reason = "";

		// 开始执行回调任务
		long beginTime = System.currentTimeMillis();

		// 执行post方法
		String uri = TaskUtil.getValue(taskParams, BussinessConstant.CALLBACK_URI);
		// uri解密
		//uri = DESUtils.decrypt(uri, null);

		try {

			// 发送请求
			String response = this.sendCallback(uri, taskParams, taskParams.get("accesskey"));
			// 接收文件请求结束
			long endPostTime = System.currentTimeMillis();
			logger.info("文件 ：[{}] 发送回调请求总共花费时间：{}ms", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY),
					(endPostTime - beginTime));
			
			if(StringUtils.isEmpty(response)) {
				logger.info("普通回调成功！uri :[{}]",uri);
				return;
			}
			
			logger.info(response);
			// 响应相应不成功
			Type respJsonType = new TypeToken<RespJson<String>>() {
			}.getType();
			RespJson<String> respJson = gson.fromJson(response, respJsonType);
			// int statusCode = response.getStatusLine().getStatusCode();
			int statusCode = respJson.getResponse().getInfo().getCode();
			if (statusCode != BussinessConstant.SUCCESS) {
				retryFlg = true;
				reason = String.format(MsgConstant.SERVER_CALLBACK_RETRY_REASON_1, uri,
						getCallbackServerInfo(taskParams), response);
			} else {
				// 回调成功
				taskDao.success(task);
				logger.info(String.format(MsgConstant.SERVER_CALLBACK_SUCCESS, new Object[] {
						TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), BussinessConstant.LOCALHOST.getHostAddress(), uri }));
			}

		} catch (Exception e) {
			String msg = String.format(MsgConstant.SERVER_CALLBACK_RETRY_REASON_2,
					new Object[] { uri, getCallbackServerInfo(taskParams) });
			logger.info(msg, e);
			retryFlg = true;
			reason = msg + e.getMessage();
		}

		// 如果需要重试
		if (retryFlg) {

			// 失败重试
			int retryTimes = task.getRetryCount();
			// 已经重试了3次，还是失败并且需要发送邮件或者短信通知
			if (retryTimes > defaultRetryTimes) {

				// 失败
				taskDao.fail(task);

				String message = String.format(MsgConstant.SERVER_CALLBACK_FAILURE,
						new Object[] { TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY),
								BussinessConstant.LOCALHOST.getHostAddress(), uri, getCallbackServerInfo(taskParams), reason });

				logger.info(message);

				// 发送通知
				noticeUtil.sendNotice(MsgConstant.CALLBACK_EXCEPTION_NOTICE_TITLE, message);

			} else {

				task.setRetryReason(reason);
				// 重试
				taskDao.retry(task);

				logger.info("文件[{}]在服务器节点[{1}]上向回调服务器[{}]发送回调请求不成功，回调服务器基础信息[{}]，已添加发送回调请求任务队列！",
						new Object[] { TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY),
								BussinessConstant.LOCALHOST.getHostAddress(), uri, getCallbackServerInfo(taskParams) });
			}
		}

		// 回调任务执行结束
		long endTime = System.currentTimeMillis();
		logger.info("文件 ：[{}] 回调队列中回调任务执行结束：{}", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), endTime);
		logger.info("文件 ：[{}] 回调任务执行总共花费时间：{}ms", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY),
				(endTime - beginTime));

	}

	/**
	 * 发送回调请求
	 *
	 * @param requestUri
	 * @param content
	 * @param accessKey
	 * @return
	 */
	private <T> String sendCallback(String requestUri, T content, Object accessKey) throws Exception {

		ReqBody<T> obj = new ReqBody<T>();
		obj.setContent(content);
		ReqCommon common = new ReqCommon();
		common.setAction(requestUri.substring(requestUri.lastIndexOf("/") + 1));
		obj.setCommon(common);

		HttpHeaders headers = new HttpHeaders();

		String requestJson = "";

		headers.set("format", "json");
		requestJson = gson.toJson(obj);
		requestJson = "{\"request\":" + requestJson + "}";
		logger.info("回调服务器地址信息：" + requestUri);
		try {
			if (accessKey != null && !"".equals(accessKey.toString())) {
				String sign = HmacSha1Util.signToString(
						Hashing.md5().hashString(requestJson, Charsets.UTF_8).toString().toLowerCase(),
						accessKey.toString(), CHARSETNAME_DEFAULT);
				headers.set("sign", URLEncoder.encode(sign, CHARSETNAME_DEFAULT));
			}
			headers.set("reqlength", requestJson.length() + "");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// post发送json
		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
		ResponseEntity<String> result = restTemplate.exchange(requestUri, HttpMethod.POST, entity, String.class);

		if (result.getStatusCode() == HttpStatus.OK) {
			return result.getBody();
		} else {
			throw new RuntimeException("回调出现异常");
		}
	}

	/**
	 * 取得回调服务器信息
	 *
	 * @param taskParams
	 *            上传用参数列表
	 * @return 回调服务器信息
	 */
	private String getCallbackServerInfo(Map<String, Object> taskParams) {

		String returnMsg = " PROVINCECODE ： " + taskParams.get("provinceCode") + ", ACCESSID : "
				+ taskParams.get("accessid") + ", ACCESSKEY : " + taskParams.get("accesskey");
		return returnMsg;
	}
}

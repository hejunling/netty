package io.hc.service.nettyTask.task;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import io.hc.service.nettyTask.cfg.TaskProperties;
import io.hc.service.nettyTask.constant.BussinessConstant;
import io.hc.service.nettyTask.constant.MsgConstant;
import io.hc.service.nettyTask.constant.ProcessEnum;
import io.hc.service.nettyTask.constant.StatusEnum;
import io.hc.service.nettyTask.constant.TaskHandleTimeEnum;
import io.hc.service.nettyTask.dao.TaskDao;
import io.hc.service.nettyTask.entity.Task;
import io.hc.service.nettyTask.event.Up2YunEvent;
import io.hc.service.nettyTask.listener.Up2YunListener;
import io.hc.service.nettyTask.utils.NoticeUtil;
import io.hc.service.nettyTask.utils.TaskUtil;
import io.hc.service.nettyTask.utils.task.HandleTask;
import io.hc.service.nettyTask.utils.task.TaskBus;

/**
 * 将文件上传到云上任务包裹类
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component
@EnableConfigurationProperties({ TaskProperties.class })
public class Up2YunTaskHandler {

	private static final Logger logger = LoggerFactory.getLogger(Up2YunTaskHandler.class);

	/** 任务持久化类Dao */
	private final TaskDao taskDao;

	/** 事件总线 */
	private final EventBus eventBus;

	/** 上传到云监听器 */
	private final Up2YunListener up2YunListener;

	/** 通知组件 */
	private final NoticeUtil noticeUtil;

	/** 默认重试次数 */
	private final int defaultRetryTimes;

	/** rest请求 */
	private final RestTemplate restTemplate;

	/**
	 * 创建实例，并将自己注册到任务总线{@link TaskBus}
	 *
	 * @param taskBus
	 *            任务总线
	 */
	@Autowired
	public Up2YunTaskHandler(TaskBus taskBus, TaskDao taskDao, EventBus eventBus, RestTemplate restTemplate,
			Up2YunListener up2YunListener, NoticeUtil noticeUtil, TaskProperties properties) {
		taskBus.register(this);
		this.taskDao = taskDao;
		this.eventBus = eventBus;
		this.up2YunListener = up2YunListener;
		this.noticeUtil = noticeUtil;
		this.defaultRetryTimes = properties.getRetryTimes();
		this.restTemplate = restTemplate;
	}

	/**
	 * 将文件上传到云
	 *
	 * @param task
	 *            任务信息实体
	 */
	@HandleTask(taskHandler = BussinessConstant.UPTOYUN, description = "将文件上传到云")
	public void upFileToYun(Task task) {

		// 默认不重试
		boolean retryFlg = false;
		// 默认不发送回调请求
		boolean callbackFlg = false;
		// 回调信息
		String callbackResult = "";
		int callbackCode = 0;

		// 取得执行参数
		Map<String, Object> taskParams = task.getParamsMap();
		// 重试原因
		String reason = "";

		// 开始执行上传任务
		long beginTime = System.currentTimeMillis();

		try {

			// 发送上传到云事件
			eventBus.post(new Up2YunEvent(taskParams));

			// 接收文件请求结束
			long endPostTime = System.currentTimeMillis();
			logger.info("文件 ：[{}] 发送上传到云事件总共花费时间：{}ms", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY),
					(endPostTime - beginTime));

			// 如果MD5值一致则上传成功
			if (up2YunListener.up2yunResult()) {

				// 上传成功信息
				callbackResult = StatusEnum.SUCCESS.getText();	
				callbackCode = StatusEnum.SUCCESS.getNum();
				// 上传OSS成功时间
				SimpleDateFormat df = new SimpleDateFormat(BussinessConstant.DEFAULT_TIME_FORMAT);// 设置日期格式
				taskParams.put("uploadTime", df.format(new Date()));

				// 上传成功发送回复信息
				String callbackUri = TaskUtil.getValue(taskParams, BussinessConstant.CALLBACK_URI);
				if (callbackUri != null && !"".equals(callbackUri)) {
					callbackFlg = true;
				}

				// 上传成功，回调成功，删除记录
				taskDao.success(task);

				// 删除文件
				restTemplate.delete(TaskUtil.getValue(taskParams, BussinessConstant.FILE_URL));

				// 如果不需要回调，则直接提示成功
				logger.info("文件[{}]在服务器节点[{}]上{}！",
						new Object[] { TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY),
								BussinessConstant.LOCALHOST.getHostAddress(), callbackResult });
			}
			// 否则重试
			else {
				retryFlg = true;
				// reason =
				// SpringContextUtil.getMessage("file.upload.retry.reason_2");
				reason = up2YunListener.getExceptionMsg();
				taskParams.put(Up2YunListener.RETRY_UP_YUN_TYPE, up2YunListener.getRetryUpYunType());
				task.setTaskParams(new Gson().toJson(taskParams));
			}

		} catch (Exception e) {
			reason = "上传到云对象存储时出现异常。" + getYunInfo(taskParams);
			logger.info(reason, e);
			retryFlg = true;
		}

		// 是否重试
		if (retryFlg) {

			// 失败重试
			int retryTimes = task.getRetryCount();
			// 上传成功发送回复信息
			String callbackUri = TaskUtil.getValue(taskParams, BussinessConstant.CALLBACK_URI);
			// 已经重试了3次，还是失败并且需要发送回调信息
			if (retryTimes > defaultRetryTimes && (callbackUri != null && !"".equals(callbackUri))) {
				callbackFlg = true;
				callbackResult = StatusEnum.FAIL.getText();
				callbackCode = StatusEnum.FAIL.getNum();
				// 上传失败
				taskDao.fail(task);

				String message = String.format(MsgConstant.FILE_UPLOAD_FAILURE,
						new Object[] { TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY),
								BussinessConstant.LOCALHOST.getHostAddress(), callbackResult, getYunInfo(taskParams), reason });
				noticeUtil.sendNotice(MsgConstant.UPLOAD_EXCEPTION_NOTICE_TITLE, message);
				logger.info(message);
			} else {

				task.setRetryReason(reason);
				// 转变重试状态
				taskDao.retry(task);

				logger.info("文件[{}]在服务器节点[{}]上{}，上传到云的信息[{}]，已添加重试上传任务队列。",
						new Object[] { TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY),
								BussinessConstant.LOCALHOST.getHostAddress(), callbackResult, getYunInfo(taskParams) });
			}
		}

		// 发送回调请求
		if (callbackFlg) {

			// 插入回调任务
			task.setTaskId(String.valueOf(UUID.randomUUID()));
			task.setReqUrl(BussinessConstant.LOCALHOST.getHostAddress());
			// 接收方uri
			String uri = TaskUtil.getValue(taskParams, BussinessConstant.CALLBACK_URI);
			// uri解密
			//uri = Strings.isNullOrEmpty(uri) ? uri : DESUtils.decrypt(uri, null);
			task.setRevUrl(uri);
			taskParams.put("callbackResult", callbackResult);
			taskParams.put("callbackCode", callbackCode);
			task.setTaskParams(new Gson().toJson(taskParams));
			task.setParamsMap(taskParams);
			task.setComputeNum(0);
			task.setTaskHandler(BussinessConstant.CALLBACK);
			task.setTaskStatus(ProcessEnum.PROCESSING.getNum());
			task.setGmtCreate(new Timestamp(System.currentTimeMillis()));
			task.setGmtHandle(new Timestamp(System.currentTimeMillis()));
			task.setHandleTimeEnum(TaskHandleTimeEnum.IMMEDIATELY);
			taskDao.addTask(task);

			logger.info("文件[{}]在服务器节点[{}]上添加进发送回调信息任务队列。", new Object[] {
					TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), BussinessConstant.LOCALHOST.getHostAddress() });
		}

		// 接收文件请求结束
		long endTime = System.currentTimeMillis();
		logger.info("文件 ：[{}] 上传队列中上传任务执行结束：{}", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), endTime);
		logger.info("文件 ：[{}] 上传任务执行总共花费时间：{}ms", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY),
				(endTime - beginTime));
	}

	/**
	 * 取得云相关信息
	 *
	 * @param taskParams
	 *            上传用参数列表
	 * @return
	 */
	private String getYunInfo(Map<String, Object> taskParams) {

		String returnMsg = "";

		// 遍历
		for (Map.Entry<String, Object> entry : taskParams.entrySet()) {
			// 记录bucket信息
			if (entry.getKey().contains("_bucket")) {
				returnMsg += ", BUCKET : " + entry.getValue().toString();
			}
			// 记录accessid
			else if (entry.getKey().contains("_accessid")) {
				returnMsg += ", ACCESSID : " + entry.getValue().toString();
			}
			// 记录accesskey
			else if (entry.getKey().contains("_accesskey")) {
				returnMsg += ", ACCESSKEY : " + entry.getValue().toString();
			}
		}

		returnMsg = returnMsg.substring(2, returnMsg.length());

		return returnMsg;
	}
}

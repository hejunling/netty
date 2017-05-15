package io.hc.service.nettyTask.listener;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.math.LongMath;

import io.hc.service.nettyTask.cfg.TaskProperties;
import io.hc.service.nettyTask.constant.BussinessConstant;
import io.hc.service.nettyTask.event.Up2YunEvent;
import io.hc.service.nettyTask.thirdparty.aliyun.AliyunOSS;
import io.hc.service.nettyTask.thirdparty.baiduyun.BaiduyunBOS;
import io.hc.service.nettyTask.thirdparty.common.Response;
import io.hc.service.nettyTask.utils.TaskUtil;

/**
 * 上传到云对象存储器事件监听
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component
@EnableConfigurationProperties({ TaskProperties.class })
public class Up2YunListener {

	private static final Logger logger = LoggerFactory.getLogger(Up2YunListener.class);

	// 分块上传默认设置每块为 20M
	private static final long PART_SIZE = 1024 * 1024 * 20;

	/** bucket后缀 */
	private static final String BUCKET_SUFFIX = "_bucket";

	/** accessid后缀 */
	private static final String ACCESSID_SUFFIX = "_accessid";

	/** accesskey后缀 */
	private static final String ACCESSKEY_SUFFIX = "_accesskey";

	/** file_path的健 */
	private static final String FILE_PATH = "file_path";

	/** user_meta_info的健 */
	private static final String USER_META_INFO = "user_meta_info";

	/** 重试上传云类型的健 */
	public static final String RETRY_UP_YUN_TYPE = "retry_up_yun_type";

	/** 默认云类型为阿里云 */
	private static final String DEFAULT_YUN = "oss";

	/** 百度云 */
	private static final String YUN_FOR_BOS = "bos";

	/** 换行符 */
	private static final String NEW_LINE = "\n";

	/** 文件保存临时目录 */
	private final String tempDir;

	/** 上传结果 */
	private List<Boolean> result = Lists.newArrayList();

	/** 异常信息 */
	private String exceptionMsg = "";

	/** 重试上传云参数 */
	private String retryUpYunType = "";

	/** rest请求 */
	private final RestTemplate restTemplate;

	@Autowired
	public Up2YunListener(EventBus bus, TaskProperties properties, RestTemplate restTemplate) {
		bus.register(this);
		this.tempDir = properties.getTempDir();
		this.restTemplate = restTemplate;
	}

	/**
	 * 上传到OSS
	 *
	 * @return
	 * @throws Exception
	 */
	@Subscribe
	public void upload2OSS(Up2YunEvent up2YunEvent) {

		// 取得相关参数
		Map<String, Object> taskParams = up2YunEvent.getTaskParams();

		try {

			// 默认上传到阿里云OSS（0：阿里云OSS）
			String yunType = TaskUtil.getValue(taskParams, BussinessConstant.YUN_TYPE);
			yunType = Strings.isNullOrEmpty(yunType) ? DEFAULT_YUN : yunType;

			// 重试上传到云
			String retryUp2YunType = TaskUtil.getValue(taskParams, RETRY_UP_YUN_TYPE);
			yunType = Strings.isNullOrEmpty(retryUp2YunType) ? yunType : retryUp2YunType;

			// 如果是上传到阿里云
			if (yunType.contains(DEFAULT_YUN)) {

				// 取得accessId
				String accessId = TaskUtil.getValue(taskParams, DEFAULT_YUN + ACCESSID_SUFFIX);

				// 取得accessKey
				String accessKey = TaskUtil.getValue(taskParams, DEFAULT_YUN + ACCESSKEY_SUFFIX);

				// 取得要传入的bucket名称
				String bucket = TaskUtil.getValue(taskParams, DEFAULT_YUN + BUCKET_SUFFIX);

				// 开始上传时间
				long beginTime = System.currentTimeMillis();
				logger.info("文件 ：[{}]上传到OSS开始：{}", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), beginTime);

				// 新建阿里云实例
				AliyunOSS oss = new AliyunOSS(accessId, accessKey);

				// 取得文件
				File file = getFile(TaskUtil.getValue(taskParams, BussinessConstant.FILE_URL));

				// 上传到阿里云
				@SuppressWarnings("unchecked")
				Response responseResult = oss.putObject(bucket, TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), file,
						file.length(), (Map<String, String>) taskParams.get(USER_META_INFO));

				// 如果成功则返回上传到阿里云后云上生成的MD5
				String getedMd5 = Strings.nullToEmpty(responseResult.getEtag());

				// 判断是否上传成功
				boolean resultFlg = fileMd5(taskParams).equals(getedMd5.toUpperCase());
				// 如果MD5验证不同过
				if (!resultFlg) {
					exceptionMsg += "上传阿里云后得到的MD5与产品平台提供的MD5不一致";
				}

				// 删除文件
				if (file.exists()) {
					file.delete();
				}

				// 上传到云结束时间
				long endTime = System.currentTimeMillis();

				logger.info("文件 ：[{}] 上传OSS结束：{}", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), endTime);
				logger.info("文件 ：[{}] 上传OSS花费时间：{}ms", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), (endTime - beginTime));

				result.add(resultFlg);
			}
		} catch (Exception e) {
			String pattern = "上传阿里云OSS时出现异常, 阿里云OSS信息[BUCKET : {0}, ACCESSID : {1}, ACCESSKEY : {2}] 具体异常信息 ：{3}";
			exceptionMsg = MessageFormat.format(pattern, TaskUtil.getValue(taskParams, DEFAULT_YUN + BUCKET_SUFFIX),
					TaskUtil.getValue(taskParams, DEFAULT_YUN + ACCESSID_SUFFIX),
					TaskUtil.getValue(taskParams, DEFAULT_YUN + ACCESSKEY_SUFFIX), e.getMessage());
			logger.info(exceptionMsg, e);
			exceptionMsg += NEW_LINE;
			result.add(false);
			// 设置重试上传云类型参数
			setRetryParams(up2YunEvent.getTaskParams(), DEFAULT_YUN);
		}
	}

	/**
	 * 上传到百度云BOS
	 *
	 * @return
	 * @throws Exception
	 */
	@Subscribe
	public void upload2BOS(Up2YunEvent up2YunEvent) {

		// 取得相关参数
		Map<String, Object> taskParams = up2YunEvent.getTaskParams();

		try {

			// 默认上传到阿里云OSS（0：阿里云OSS）
			String yunType = TaskUtil.getValue(taskParams, BussinessConstant.YUN_TYPE);
			yunType = Strings.isNullOrEmpty(yunType) ? DEFAULT_YUN : yunType;

			// 重试上传到云
			String retryUp2YunType = TaskUtil.getValue(taskParams, RETRY_UP_YUN_TYPE);
			yunType = Strings.isNullOrEmpty(retryUp2YunType) ? yunType : retryUp2YunType;

			// 如果是上传到阿里云
			if (yunType.contains(YUN_FOR_BOS)) {

				// 取得accessId
				String accessId = TaskUtil.getValue(taskParams, YUN_FOR_BOS + ACCESSID_SUFFIX);

				// 取得accessKey
				String accessKey = TaskUtil.getValue(taskParams, YUN_FOR_BOS + ACCESSKEY_SUFFIX);

				// 取得要传入的bucket名称
				String bucket = TaskUtil.getValue(taskParams, YUN_FOR_BOS + BUCKET_SUFFIX);

				// 新建百度云BOSclient实例
				BaiduyunBOS oss = new BaiduyunBOS(accessId, accessKey);

				File file = getFile(TaskUtil.getValue(taskParams, BussinessConstant.FILE_URL));

				// 开始上传时间
				long beginTime = System.currentTimeMillis();
				logger.info("文件 ：[{}]上传到BOS开始：{}", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), beginTime);

				// 上传到阿里云
				@SuppressWarnings("unchecked")
				Response responseResult = oss.putObject(bucket, TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), file,
						(Map<String, String>) taskParams.get(USER_META_INFO));

				// 如果成功则返回上传到阿里云后云上生成的MD5
				String getedMd5 = Strings.nullToEmpty(responseResult.getEtag());

				// 判断是否上传成功
				boolean resultFlg = TaskUtil.getValue(taskParams, BussinessConstant.FILE_MD5).toUpperCase()
						.equals(getedMd5.toUpperCase());
				// 如果MD5验证不同过
				if (!resultFlg) {
					exceptionMsg += "上传百度云后得到的MD5与产品平台提供的MD5不一致";
				}

				// 删除文件
				if (file.exists()) {
					file.delete();
				}

				// 上传到云结束时间
				long endTime = System.currentTimeMillis();
				logger.info("文件 ：[{}] 上传BOS结束：{}", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), endTime);
				logger.info("文件 ：[{}] 上传BOS花费时间：{}ms", TaskUtil.getValue(taskParams, BussinessConstant.FILE_KEY), (endTime - beginTime));

				result.add(resultFlg);
			}
		} catch (Exception e) {
			String pattern = "上传百度云BOS时出现异常, 百度云BOS信息[BUCKET : {0}, ACCESSID : {1}, ACCESSKEY : {2}] 具体异常信息 ：{3}";
			exceptionMsg = MessageFormat.format(pattern, TaskUtil.getValue(taskParams, YUN_FOR_BOS + BUCKET_SUFFIX),
					TaskUtil.getValue(taskParams, YUN_FOR_BOS + ACCESSID_SUFFIX),
					TaskUtil.getValue(taskParams, YUN_FOR_BOS + ACCESSKEY_SUFFIX), e.getMessage());
			logger.info(exceptionMsg, e);
			exceptionMsg += NEW_LINE;
			result.add(false);
			// 设置重试上传云类型参数
			setRetryParams(up2YunEvent.getTaskParams(), YUN_FOR_BOS);
		}
	}

	/**
	 * 取得上传到云结果
	 *
	 * @return 结果
	 */
	public boolean up2yunResult() {

		// 结果
		boolean flg = true;

		// 遍历列表求出结果
		for (Boolean b : result) {
			flg = flg && b;
		}

		// 如果有结果则清空，否则结果为false
		if (result.size() > 0) {
			// 清空结果
			result.clear();
		} else {
			flg = false;
		}

		return flg;
	}

	/**
	 * 取得异常信息
	 *
	 * @return 异常信息
	 */
	public String getExceptionMsg() {

		String returnMsg = exceptionMsg;

		exceptionMsg = null;

		return returnMsg;
	}

	/**
	 * 取得重试上传参数
	 *
	 * @return 重试上传参数
	 */
	public String getRetryUpYunType() {

		String returnType = retryUpYunType;

		retryUpYunType = null;

		return returnType;
	}

	/**
	 * 设置重试上传云参数
	 *
	 * @param taskParams
	 * @param yunType
	 */
	private void setRetryParams(Map<String, Object> taskParams, String yunType) {
		retryUpYunType = TaskUtil.getValue(taskParams, RETRY_UP_YUN_TYPE);
		retryUpYunType = retryUpYunType.contains(yunType) ? retryUpYunType : retryUpYunType + "," + yunType;
	}

	/**
	 * 取得文件
	 *
	 * @param fileUrl
	 *            文件网络路径
	 * @return 文件
	 */
	private File getFile(String fileUrl) {

		logger.info("fileUrl = [{}]",fileUrl);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));

		HttpEntity<String> entity = new HttpEntity<String>(headers);

		ResponseEntity<byte[]> response = restTemplate.exchange(fileUrl, HttpMethod.GET, entity, byte[].class);

		// 取得文件
		File file = new File(tempDir + UUID.randomUUID().toString());

		if (response.getStatusCode() == HttpStatus.OK) {

			try {
				Files.write(response.getBody(), file);
			} catch (IOException e) {
				logger.error("文件写入异常", e);
			}

		}

		return file;

	}

	/**
	 * 计算接收文件的MD5值
	 *
	 * @param taskParams
	 *            参数
	 * @return 接收文件的MD5
	 */
	private String fileMd5(Map<String, Object> taskParams) throws Exception {
		// 从客户端接收md5
		String md5 = TaskUtil.getValue(taskParams, BussinessConstant.FILE_MD5).toUpperCase();

		// 文件路径
		String filePath = TaskUtil.getValue(taskParams, FILE_PATH);
		File file = new File(filePath);
		long fileLength = file.length();
		// 大于20M分块上传
		if (fileLength > PART_SIZE) {
			// 将md5置为空
			md5 = "";
			// 取得分块数
			long partNum = LongMath.divide(fileLength, PART_SIZE, RoundingMode.UP);
			// 取得文件源
			ByteSource byteSource = Files.asByteSource(file);
			for (int i = 0; i < partNum; i++) {
				long offset = i * PART_SIZE;
				long length = PART_SIZE;
				if ((offset + length) > fileLength) {
					length = fileLength - offset;
				}
				ByteSource byteSourceTemp = byteSource.slice(offset, length);
				md5 += Hashing.md5().hashBytes(byteSourceTemp.read()).toString().toUpperCase();
			}
			md5 = Hashing.md5().hashBytes(md5.getBytes()).toString().toUpperCase() + "-" + partNum;
		}

		return md5;
	}
}

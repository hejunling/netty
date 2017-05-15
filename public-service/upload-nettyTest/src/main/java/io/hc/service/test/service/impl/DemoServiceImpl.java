package io.hc.service.test.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import io.hc.service.test.config.NettyUploadConfig;
import io.hc.service.test.service.IDemoService;
import io.hc.service.test.utils.HttpClientUtils;
import io.hc.service.test.utils.MD5Utils;

@Service
public class DemoServiceImpl implements IDemoService{
	
	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(IDemoService.class);

	@Autowired
	private NettyUploadConfig config;

	@Override
	public void upload() {
		logger.info("start...");

		// E:/Z-资料/规划成长.zip
		String fileName = "E:/Z-资料/规划成长04.zip";

		// 回调地址
		String callbackurl = config.getCallbackUrl();

		// oss bucket
		String aliyunbucket = config.getOssBuket();

		// 存储文件名
		String aliyunobject = System.currentTimeMillis() + "guihua-grown.zip";

		// 本地md5值
		String localMd5 = MD5Utils.md5file(fileName);

		// 需要上传的文件
		File file = new File(fileName);

		// 准备需要的参数
		Map<String, Object> paramMap = Maps.newHashMap();

		// 上传时的参数，有些参数是回调的时候要用的，可以在这里输入，回调时直接传回
		paramMap.put("oss_bucket", aliyunbucket);
		paramMap.put("fileKey", aliyunobject);
		paramMap.put("fileMd5", localMd5);
		paramMap.put("fileSize", file.length());
		paramMap.put("oss_accessid", config.getAccessId());
		paramMap.put("oss_accesskey", config.getAccessKey());
		paramMap.put("callbackUri", callbackurl);

		// 上传组件回调回来时使用
		paramMap.put("licenceno", "licenceno");

		// 上传组件的URI
		String result = null;
		try {
			logger.info("paramMap = {}", paramMap);
			// POST方式发送文件到上传组件
			// uploadUri = "http://127.0.0.1:8090";
			result = HttpClientUtils.postFile2String(config.getUploadUrl(), file, paramMap, "UTF-8");
			logger.info(result);
		} catch (Exception e) {
			throw new RuntimeException("上传到netty异常...", e);
		} finally {
			// 删除加密文件
			try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				logger.error("删除文件异常...filename = {}", fileName, e);
			}
		}

		logger.info("end...");
	}
	
	@Override
	public void callback() {
		
		logger.debug("exectue....business service application");
	}
}

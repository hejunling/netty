package io.hc.service.test.config;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.MoreObjects;

/**
 * 上传配置
 * 
 * @author hechuan
 *
 * @created 2017年5月10日
 *
 * @since UPLOAD-2.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun.upload")
public class NettyUploadConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** oss-bukcet */
	private String ossBuket;

	/** oss-accessId */
	private String accessId;

	/** oss-accessKey */
	private String accessKey;

	/** 回调路径 */
	private String callbackUrl;

	/** 暂时路径 */
	private String tempFilePath;

	/** 上传路径 */
	private String uploadUrl;

	public String getOssBuket() {
		return ossBuket;
	}

	public void setOssBuket(String ossBuket) {
		this.ossBuket = ossBuket;
	}

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public String getTempFilePath() {
		return tempFilePath;
	}

	public void setTempFilePath(String tempFilePath) {
		this.tempFilePath = tempFilePath;
	}

	public String getUploadUrl() {
		return uploadUrl;
	}

	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(NettyUploadConfig.class).add("ossBucket", ossBuket).add("accessId", accessId)
				.add("accessKey", accessKey).add("callbackUrl", callbackUrl).add("tempFilePath", tempFilePath)
				.add("uploadUrl", uploadUrl).toString();
	}

}

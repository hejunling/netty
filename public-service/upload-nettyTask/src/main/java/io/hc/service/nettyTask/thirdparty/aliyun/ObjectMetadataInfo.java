package io.hc.service.nettyTask.thirdparty.aliyun;

import java.util.Map;

import com.aliyun.oss.model.ObjectMetadata;

/**
 * 对象元数据信息
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-2.0.0
 */
public class ObjectMetadataInfo extends ObjectMetadata {
	private Map<String, Object> rawMatadata;

	public Map<String, Object> getRawMatadata() {
		return rawMatadata;
	}

	public void setRawMatadata(Map<String, Object> rawMatadata) {
		this.rawMatadata = rawMatadata;
	}
}

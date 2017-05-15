package io.hc.service.nettyTask.event;

import java.util.HashMap;
import java.util.Map;

/**
 * 上传到云对象存储器事件
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class Up2YunEvent {

	/** 上传所需的参数 */
	private Map<String, Object> up2yunParams = new HashMap<String, Object>();

	/**
	 * 构造事件
	 *
	 * @param message
	 */
	public Up2YunEvent(Map<String, Object> message) {
		up2yunParams = message;
	}

	/**
	 * 取得传递的参数
	 *
	 * @return
	 */
	public Map<String, Object> getTaskParams() {
		return up2yunParams;
	}

}

package io.hc.service.nettyTask.server.scan;

import org.springframework.stereotype.Component;

import io.hc.service.nettyTask.server.DefaultServerListener;

/**
 * 扫描服务默认监听器
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component
public class ScanServerListener extends DefaultServerListener {

	/**
	 * 初始化扫描服务默认监听器
	 */
	public ScanServerListener() {
		this("定时扫描");
	}

	/**
	 * 初始化扫描服务默认监听器
	 *
	 * @param serverName
	 *            服务名
	 */
	public ScanServerListener(String serverName) {
		super(serverName);
	}
}

package io.hc.service.nettyTask.server.monitor;

import org.springframework.stereotype.Component;

import io.hc.service.nettyTask.server.DefaultServerListener;

/**
 * 应用监控服务默认监听器
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component
public class MonitorServerListener extends DefaultServerListener {

	/**
	 * 初始化应用监控服务默认监听器
	 */
	public MonitorServerListener() {
		this("应用监控");
	}

	/**
	 * 初始化应用监控服务默认监听器
	 *
	 * @param serverName
	 *            服务名
	 */
	public MonitorServerListener(String serverName) {
		super(serverName);
	}
}

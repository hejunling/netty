package io.hc.service.nettyTask.server.taskstatus;

import org.springframework.stereotype.Component;

import io.hc.service.nettyTask.server.DefaultServerListener;

/**
 * 任务状态重置服务默认监听器
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component
public class TaskStatusResetServerListener extends DefaultServerListener {

	/**
	 * 任务状态重置服务监听器
	 */
	public TaskStatusResetServerListener() {
		this("任务状态重置");
	}

	/**
	 * 任务状态重置服务监听器
	 *
	 * @param serverName
	 *            服务名
	 */
	public TaskStatusResetServerListener(String serverName) {
		super(serverName);
	}
}

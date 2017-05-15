package io.hc.service.nettyTask.event;

import io.hc.service.nettyTask.entity.Task;

/**
 * 任务进入队列事件
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class InQueneEvent {

	/** 任务实体 */
	private Task task;

	/**
	 * 构造函数
	 *
	 * @param task
	 *            任务实体
	 */
	public InQueneEvent(Task task) {
		this.task = task;
	}

	/**
	 * 取得任务实体
	 *
	 * @return 任务实体
	 */
	public Task getTask() {
		return task;
	}
}

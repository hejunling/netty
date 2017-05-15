package io.hc.service.nettyTask.event;

import io.hc.service.nettyTask.entity.Task;

/**
 * 任务事件
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class TaskEvent {

	/** 异常实体 */
	private final Task task;

	/**
	 * 构造函数
	 *
	 * @param task
	 */
	public TaskEvent(Task task) {
		this.task = task;
	}

	/**
	 * 取得任务实体
	 *
	 * @return 取得任务实体
	 */
	public Task getTask() {
		return this.task;
	}

}

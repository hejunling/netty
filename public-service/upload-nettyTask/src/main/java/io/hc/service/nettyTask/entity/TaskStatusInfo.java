package io.hc.service.nettyTask.entity;

import com.google.common.base.MoreObjects;

/**
 * 任务状态信息
 * 
 * @author hechuan
 *
 * @created 2017年4月12日
 *
 * @since UPLOAD-3.0.0
 */
public class TaskStatusInfo {

	/** 节点编号 */
	private int computeNum;

	/** 任务数 */
	private long taskNum;

	public long getTaskNum() {
		return taskNum;
	}

	public void setTaskNum(long taskNum) {
		this.taskNum = taskNum;
	}

	public int getComputeNum() {
		return computeNum;
	}

	public void setComputeNum(int computeNum) {
		this.computeNum = computeNum;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("computeNum", computeNum).add("taskNum", taskNum).toString();
	}
}

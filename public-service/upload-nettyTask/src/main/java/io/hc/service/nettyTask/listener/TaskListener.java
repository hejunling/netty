package io.hc.service.nettyTask.listener;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.hc.service.nettyTask.entity.Task;
import io.hc.service.nettyTask.event.InQueneEvent;
import io.hc.service.nettyTask.utils.TaskUtil;
import io.hc.service.nettyTask.utils.task.TaskBus;

/**
 * 任务监听器，如果有任务时压到任务队列
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class TaskListener {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(TaskListener.class);

	/** 任务总线 */
	private final TaskBus taskBus;

	/** 设定好的间隔时间 */
	private final long settingDuration;

	/**
	 * 构造函数
	 *
	 * @param eventBus
	 *            事件总线
	 */
	public TaskListener(EventBus eventBus, TaskBus taskBus, long settingDuration) {
		eventBus.register(this);
		this.taskBus = taskBus;
		this.settingDuration = settingDuration;
	}

	/**
	 * 将任务压进队列
	 *
	 * @param inQueneEvent
	 *            进队列事件
	 */
	@Subscribe
	public void pushTaskInQuene(InQueneEvent inQueneEvent) {

		// 取得任务
		Task task = inQueneEvent.getTask();

		// 添加任务事件执行
		long beginInQueneTime = System.currentTimeMillis();
		// TODO 暂时对应策略
		logger.info("文件 ：[{}] 任务进队列执行开始：[{}]", TaskUtil.getValue(task.getParamsMap(), "fileKey"), beginInQueneTime);

		// 计算多少时间内重做=已经重做的次数*重做的时间延迟
		long duration = task.getRetryCount() * settingDuration;

		// 是否重试
		boolean retryFlg = task.getRetryCount() > 0;

		// 执行任务
		taskBus.handlerTask(retryFlg, task.getTaskHandler(), task, duration, TimeUnit.MILLISECONDS);

		logger.info("任务总线信息：{}", taskBus);
	}

	/**
	 * 停止
	 */
	public void stop() {

	}

}

package io.hc.service.nettyTask.server.taskstatus;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.MoreExecutors;

import io.hc.service.nettyTask.cfg.TaskProperties;
import io.hc.service.nettyTask.server.ServerManager;
import io.hc.service.nettyTask.service.ScanService;
import io.hc.service.nettyTask.strategy.Strategy;

/**
 * 任务状态重置服务
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component
@EnableConfigurationProperties({ TaskProperties.class })
public class TaskStatusResetServer {

	private static final Logger logger = LoggerFactory.getLogger(TaskStatusResetServer.class);

	/** 任务相关配置 */
	@Resource
	private TaskProperties properties;

	/** 是不是第一次扫描 */
	private static boolean firstScanFlg = true;

	/** 扫描service */
	@Resource
	private ScanService scanService;

	/** 策略类 */
	@Resource
	private Strategy strategy;

	/**
	 * 构建任务状态重置服务实例 1.为该服务添加监听 2.将该服务添加到服务管理
	 *
	 * @param serverManager
	 *            服务管理
	 * @param listener
	 *            监听
	 */
	@Autowired
	public TaskStatusResetServer(ServerManager serverManager, TaskStatusResetServerListener listener) {
		// 创建服务实例
		TaskStatusResetService service = new TaskStatusResetService();

		// 添加监听
		service.addListener(listener, MoreExecutors.directExecutor());

		// 将服务注册到服务管理集中
		serverManager.register(service);
	}

	/**
	 * 任务状态初始化线程
	 */
	private class TaskStatusResetService extends AbstractScheduledService {

		/**
		 * 重置任务状态操作
		 *
		 * @throws Exception
		 */
		@Override
		protected void runOneIteration() throws Exception {
			logger.debug("扫描需要重置的任务...");
			int count = scanService.resetTask(firstScanFlg, strategy.getStrategy());
			logger.debug("已重置了{}个任务。", count);
			firstScanFlg = false;
		}

		/**
		 * 定时器
		 *
		 * @return 定时器
		 */
		@Override
		protected Scheduler scheduler() {
			return Scheduler.newFixedRateSchedule(properties.getDelayTime(), properties.getStatusTime(),
					TimeUnit.MILLISECONDS);
		}
	}
}

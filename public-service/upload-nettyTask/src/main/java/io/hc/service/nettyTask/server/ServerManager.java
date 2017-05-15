package io.hc.service.nettyTask.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 自定义服务管理集
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component
@Order(Integer.MAX_VALUE)
public class ServerManager implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(ServerManager.class);

	/** 服务列表 */
	private Set<Service> servers = Sets.newConcurrentHashSet();

	/** 服务管理类 */
	private ServiceManager manager;

	/** 服务管理类监听器 */
	private final ServiceManager.Listener managerListener;

	/**
	 * 创建服务管理集，并使用默认监听器
	 */
	public ServerManager() {
		this(null);
	}

	/**
	 * 创建服务管理集，并初始化监听器
	 *
	 * @param listener
	 *            自定义监听器
	 */
	public ServerManager(ServiceManager.Listener listener) {
		this.managerListener = MoreObjects.firstNonNull(listener, new DefaultManagerListener());
	}

	/**
	 * 将一个服务注册到服务列表
	 *
	 * @param service
	 *            服务
	 */
	public void register(Service service) {
		servers.add(service);
	}

	/**
	 * 启动服务管理集合中的所有服务
	 */
	public void startAllServer() {

		// 如果服务管理集不存在
		if (this.manager == null) {
			// 创建服务管理集实例
			this.manager = new ServiceManager(servers);
			// 为服务集添加监听器
			manager.addListener(this.managerListener, MoreExecutors.directExecutor());
		}

		// 启动服务集中的所有服务
		manager.startAsync().awaitStopped();
	}

	/**
	 * 停止服务管理集合中的所有服务
	 */
	public void stopAllServer() {
		if (this.manager != null) {
			this.manager.stopAsync();
		}
	}

	/**
	 * 当所有bean都实例化好时，启动任务
	 *
	 * @param strings
	 *            相关参数
	 * @throws Exception
	 *             异常
	 */
	@Override
	public void run(String... strings) throws Exception {
		this.startAllServer();
	}

	@Override
	public String toString() {
		return "ServerManager{" + "manager=" + manager + '}';
	}

	/**
	 * 默认监听器
	 */
	private static class DefaultManagerListener extends ServiceManager.Listener {

		@Override
		public void healthy() {
			logger.info("服务被管理中...");
		}

		@Override
		public void stopped() {
			logger.info("服务管理监测到服务已停止");
		}

		@Override
		public void failure(Service service) {
			logger.info("{}服务运行出错", service);
		}
	}
}

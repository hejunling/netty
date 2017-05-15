package io.hc.service.nettyTask.server;

import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认监听器
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class DefaultServerListener extends Service.Listener {

	/** 日志类 */
	protected final Logger logger;

	/** 服务名 */
	protected final String serverName;

	/**
	 * 初始化默认监听器
	 *
	 * @param serverName
	 *            服务名
	 */
	public DefaultServerListener(String serverName) {
		this.serverName = serverName;
		logger = LoggerFactory.getLogger(getClass());
	}

	/**
	 * 服务启动
	 */
	@Override
	public void starting() {
		logger.info("{}服务开始启动.....", serverName);
	}

	/**
	 * 服务运行中
	 */
	@Override
	public void running() {
		logger.info("{}服务开始运行", serverName);
	}

	/**
	 * 服务关闭中...
	 *
	 * @param from
	 *            服务状态枚举
	 */
	@Override
	public void stopping(Service.State from) {
		logger.info("{}服务关闭中.....", serverName);
	}

	/**
	 * 服务终止时
	 *
	 * @param from
	 *            服务状态枚举
	 */
	@Override
	public void terminated(Service.State from) {
		logger.info("{}服务终止", serverName);
	}

	/**
	 * 服务出现异常时
	 *
	 * @param from
	 *            服务状态枚举
	 * @param failure
	 *            具体异常
	 */
	@Override
	public void failed(Service.State from, Throwable failure) {
		logger.info("{}服务发生异常，cause：{}", serverName, failure.getCause());
	}

}

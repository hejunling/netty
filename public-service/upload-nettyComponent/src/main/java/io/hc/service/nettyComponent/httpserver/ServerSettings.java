package io.hc.service.nettyComponent.httpserver;

/**
 * @author toddf
 * @since May 31, 2012
 */
public class ServerSettings {
	private static final int DEFAULT_IO_THREAD_COUNT = 0;
	private static final int DEFAULT_EXECUTOR_THREAD_POOL_SIZE = 10;
	private static final int DEFAULT_MAX_CONTENT_SIZE = 25600;

	private String name;
	private int port;
	private String hostname;
	private boolean keepAlive = true;
	private boolean reuseAddress = true;
	/** http请求体的最大值(默认) */
	private int maxContentSize = DEFAULT_MAX_CONTENT_SIZE;

	// This controls the number of concurrent connections the application can
	// handle.
	// Netty default is 2 * number of processors (or cores).
	// Zero (0) indicates to use the Netty default.
	private int ioThreadCount = DEFAULT_IO_THREAD_COUNT;

	// This controls the size of the thread pool for back-end executors. In
	// essence,
	// this is the number of blocking requests the application can process
	// simultaneously.
	private int executorThreadPoolSize = DEFAULT_EXECUTOR_THREAD_POOL_SIZE;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean isKeepAlive) {
		this.keepAlive = isKeepAlive;
	}

	public boolean isReuseAddress() {
		return reuseAddress;
	}

	public void setReuseAddress(boolean reuseAddress) {
		this.reuseAddress = reuseAddress;
	}

	public int getIoThreadCount() {
		return ioThreadCount;
	}

	public void setIoThreadCount(int ioThreadCount) {
		this.ioThreadCount = ioThreadCount;
	}

	public int getExecutorThreadPoolSize() {
		return executorThreadPoolSize;
	}

	public void setExecutorThreadPoolSize(int executorThreadCount) {
		this.executorThreadPoolSize = executorThreadCount;
	}

	public String getHostname() {
		return hostname;
	}

	public boolean hasHostname() {
		return (hostname != null);
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getMaxContentSize() {
		return maxContentSize;
	}

	public void setMaxContentSize(int maxContentSize) {
		this.maxContentSize = maxContentSize;
	}
}

package io.hc.service.nettyBoot.cfg;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * netty相关配置
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {

	/** netty服务名 */
	private String name = "NETTY_SERVER";

	/** 端口 */
	private int port = 8080;

	/** io线程数 */
	private int workerThreads = 20;

	/** 请求体最大大小 */
	private long maxContentSize = 104857600;

	/** 上传文件保存用临时文件夹 */
	private String tempDir = System.getProperty("user.dir") + "\\temp";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getWorkerThreads() {
		return workerThreads;
	}

	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}

	public long getMaxContentSize() {
		return maxContentSize;
	}

	public void setMaxContentSize(long maxContentSize) {
		this.maxContentSize = maxContentSize;
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}
}

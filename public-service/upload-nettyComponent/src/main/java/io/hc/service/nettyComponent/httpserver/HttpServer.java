package io.hc.service.nettyComponent.httpserver;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hc.service.nettyComponent.common.NettyBootstrapFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * netty 构建httpServer
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-3.0.0
 */
public class HttpServer {

	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

	public static final String DEFAULT_NAME = "HttpServer";
	public static final int DEFAULT_PORT = 8081;

	private static final ChannelGroup allChannels = new DefaultChannelGroup(DEFAULT_NAME, GlobalEventExecutor.INSTANCE);
	private NettyBootstrapFactory bootstrapFactory = new NettyBootstrapFactory();
	private HttpServerInitializer httpServerInitializer = new HttpServerInitializer();
	private ServerSettings serverSettings = new ServerSettings();
	private SocketSettings socketSettings = new SocketSettings();

	public static HttpServer builder() {
		return new HttpServer();
	}

	/**
	 * Create a new HttpServer service. By default, HttpServer uses port 8081.
	 * Supports JSON, and XML, providing JSEND-style wrapped responses. And
	 * displays some messages on System.out. These can be altered with the
	 * setPort(), noJson(), noXml(), noSystemOut(), and useRawResponses() DSL
	 * modifiers, respectively, as needed.
	 * 
	 * <p/>
	 * The default input and output format for messages is JSON. To change that,
	 * use the setDefaultFormat(String) DSL modifier, passing the format to use
	 * by default. Make sure there's a corresponding SerializationProcessor for
	 * that particular format. The Format class has the basics.
	 * 
	 * <p/>
	 * This DSL was created as a thin veneer on Netty functionality. The bind()
	 * method simply builds a Netty pipeline and uses this builder class to
	 * create it. Underneath the covers, HttpServer uses Google GSON for JSON
	 * handling and XStream for XML processing. However, both of those can be
	 * swapped out using the putSerializationProcessor(String,
	 * SerializationProcessor) method, creating your own instance of
	 * SerializationProcessor as necessary.
	 */
	public HttpServer() {
		super();
		setName(DEFAULT_NAME);
	}

	/**
	 * Get the name of this HttpServer service.
	 *
	 * @return a String representing the name of this service suite.
	 */
	public String getName() {
		return serverSettings.getName();
	}

	/**
	 * Set the name of this HttpServer service suite.
	 *
	 * @param name
	 *            the name.
	 * @return the HttpServer instance to facilitate DSL-style method chaining.
	 */
	public HttpServer setName(String name) {
		serverSettings.setName(name);
		return this;
	}

	public int getPort() {
		return serverSettings.getPort();
	}

	public HttpServer setPort(int port) {
		serverSettings.setPort(port);
		return this;
	}

	public String getHostname() {
		return serverSettings.getHostname();
	}

	public boolean hasHostname() {
		return serverSettings.hasHostname();
	}

	/**
	 * Set the hostname or IP address that the server will listen on.
	 * 
	 * @param hostname
	 *            hostname or IP address.
	 */
	public void setHostname(String hostname) {
		serverSettings.setHostname(hostname);
	}

	public boolean useKeepAlive() {
		return serverSettings.isKeepAlive();
	}

	public HttpServer setKeepAlive(boolean useKeepAlive) {
		serverSettings.setKeepAlive(useKeepAlive);
		return this;
	}

	public boolean shouldReuseAddress() {
		return serverSettings.isReuseAddress();
	}

	public HttpServer setReuseAddress(boolean reuseAddress) {
		serverSettings.setReuseAddress(reuseAddress);
		return this;
	}

	/**
	 * Return the number of requested NIO/HTTP-handling worker threads.
	 *
	 * @return the number of requested worker threads.
	 */
	public int getIoThreadCount() {
		return serverSettings.getIoThreadCount();
	}

	/**
	 * Set the number of NIO/HTTP-handling worker threads. This value controls
	 * the number of simultaneous connections the application can handle.
	 * 
	 * The default (if this value is not set, or set to zero) is the Netty
	 * default, which is 2 times the number of processors (or cores).
	 * 
	 * @param value
	 *            the number of desired NIO worker threads.
	 * @return the HttpServer instance.
	 */
	public HttpServer setIoThreadCount(int value) {
		serverSettings.setIoThreadCount(value);
		return this;
	}

	/**
	 * Returns the number of background request-handling (executor) threads.
	 *
	 * @return the number of executor threads.
	 */
	public int getExecutorThreadCount() {
		return serverSettings.getExecutorThreadPoolSize();
	}

	/**
	 * Set the number of background request-handling (executor) threads. This
	 * value controls the number of simultaneous blocking requests that the
	 * server can handle. For longer-running requests, a higher number may be
	 * indicated.
	 * 
	 * For VERY short-running requests, a value of zero will cause no background
	 * threads to be created, causing all processing to occur in the NIO
	 * (front-end) worker thread.
	 * 
	 * @param value
	 *            the number of executor threads to create.
	 * @return the HttpServer instance.
	 */
	public HttpServer setExecutorThreadCount(int value) {
		serverSettings.setExecutorThreadPoolSize(value);
		httpServerInitializer.setExecutionHandler(initializeExecutorGroup());
		return this;
	}

	/**
	 * Set the maximum length of the content in a request. If the length of the
	 * content exceeds this value, the server closes the connection immediately
	 * without sending a response.
	 * 
	 * @param size
	 *            the maximum size in bytes.
	 * @return the HttpServer instance.
	 */
	public HttpServer setMaxContentSize(int size) {
		// 如果长度为0使用默认值
		if (size > 0) {
			serverSettings.setMaxContentSize(size);
		}
		return this;
	}

	public boolean useTcpNoDelay() {
		return socketSettings.useTcpNoDelay();
	}

	public HttpServer setUseTcpNoDelay(boolean useTcpNoDelay) {
		socketSettings.setUseTcpNoDelay(useTcpNoDelay);
		return this;
	}

	public int getSoLinger() {
		return socketSettings.getSoLinger();
	}

	public HttpServer setSoLinger(int soLinger) {
		socketSettings.setSoLinger(soLinger);
		return this;
	}

	public int getReceiveBufferSize() {
		return socketSettings.getReceiveBufferSize();
	}

	public HttpServer setReceiveBufferSize(int receiveBufferSize) {
		socketSettings.setReceiveBufferSize(receiveBufferSize);
		return this;
	}

	public int getConnectTimeoutMillis() {
		return socketSettings.getConnectTimeoutMillis();
	}

	public HttpServer setConnectTimeoutMillis(int connectTimeoutMillis) {
		socketSettings.setConnectTimeoutMillis(connectTimeoutMillis);
		return this;
	}

	public HttpServer addRequestHandler(ChannelHandler handler) {
		httpServerInitializer.addRequestHandler(handler);
		return this;
	}

	public HttpServer setExecutionHandler(EventExecutorGroup executorGroup) {
		httpServerInitializer.setExecutionHandler(executorGroup);
		return this;
	}

	public HttpServer setSSLContext(boolean ssl) throws SSLException, CertificateException {
		// Configure SSL.
		if (ssl) {

			SelfSignedCertificate ssc = new SelfSignedCertificate();
			SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			httpServerInitializer.setSslContext(sslCtx);

		}
		return this;
	}

	public SslContext getSSLContext() {
		return httpServerInitializer.getSslContext();
	}

	public Channel bind() {
		return bind((getPort() > 0 ? getPort() : DEFAULT_PORT));
	}

	/**
	 * The last call in the building of a HttpServer server, bind() causes Netty
	 * to bind to the listening address and process incoming messages.
	 *
	 * @return Channel netty启动后通道流
	 */
	/**
	 * The last call in the building of a HttpServer server, bind() causes Netty
	 * to bind to the listening address and process incoming messages.
	 *
	 * @param port
	 *            端口
	 * @return netty启动后通道流
	 */
	public Channel bind(int port) {
		setPort(port);

		if (hasHostname()) {
			return bind(new InetSocketAddress(getHostname(), port));
		}

		return bind(new InetSocketAddress(port));
	}

	/**
	 * Bind to a particular hostname or IP address and port.
	 * 
	 * @param hostname
	 *            主机名
	 * @param port
	 *            端口号
	 * @return netty启动后通道流
	 */
	public Channel bind(String hostname, int port) {
		setPort(port);
		return bind(new InetSocketAddress(hostname, port));
	}

	public Channel bind(InetSocketAddress ipAddress) {
		ServerBootstrap bootstrap = bootstrapFactory.newServerBootstrap(getIoThreadCount());
		bootstrap.childHandler(httpServerInitializer.setMaxContentLength(serverSettings.getMaxContentSize()));

		setBootstrapOptions(bootstrap);

		Channel channel = bootstrap.bind(ipAddress).channel();
		allChannels.add(channel);
		logger.info("netty服务已启动，监听端口：{}", this.getPort());

		return channel;
	}

	private EventExecutorGroup initializeExecutorGroup() {
		if (getExecutorThreadCount() > 0) {
			return new DefaultEventExecutorGroup(getExecutorThreadCount());
		}

		return null;
	}

	private void setBootstrapOptions(ServerBootstrap bootstrap) {
		bootstrap.option(ChannelOption.SO_KEEPALIVE, useKeepAlive());
		bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		bootstrap.option(ChannelOption.TCP_NODELAY, useTcpNoDelay());
		bootstrap.option(ChannelOption.SO_KEEPALIVE, serverSettings.isKeepAlive());
		bootstrap.option(ChannelOption.SO_REUSEADDR, shouldReuseAddress());
		bootstrap.option(ChannelOption.SO_LINGER, getSoLinger());
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeoutMillis());
		bootstrap.option(ChannelOption.SO_RCVBUF, getReceiveBufferSize());
		bootstrap.option(ChannelOption.MAX_MESSAGES_PER_READ, Integer.MAX_VALUE);

		bootstrap.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true));
		bootstrap.childOption(ChannelOption.MAX_MESSAGES_PER_READ, Integer.MAX_VALUE);
		bootstrap.childOption(ChannelOption.SO_RCVBUF, getReceiveBufferSize());
		bootstrap.childOption(ChannelOption.SO_REUSEADDR, shouldReuseAddress());
	}

	/**
	 * Releases all resources associated with this server so the JVM can
	 * shutdown cleanly. Call this method to finish using the server. To utilize
	 * the default shutdown hook in main() provided by HttpServer, call
	 * awaitShutdown() instead.
	 * <p/>
	 * Same as shutdown(false);
	 */
	public void shutdown() {
		shutdown(false);
	}

	/**
	 * Releases all resources associated with this server so the JVM can
	 * shutdown cleanly. Call this method to finish using the server. To utilize
	 * the default shutdown hook in main() provided by HttpServer, call
	 * awaitShutdown() instead.
	 * 
	 * @param shouldWait
	 *            true if shutdown() should wait for the shutdown of each thread
	 *            group.
	 */
	public void shutdown(boolean shouldWait) {
		ChannelGroupFuture channelFuture = allChannels.close();
		bootstrapFactory.shutdownGracefully(shouldWait);
		channelFuture.awaitUninterruptibly();
	}
}

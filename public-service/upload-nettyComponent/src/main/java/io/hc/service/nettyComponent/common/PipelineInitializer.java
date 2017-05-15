package io.hc.service.nettyComponent.common;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * netty进出管道初始化
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-3.0.0
 */
public abstract class PipelineInitializer extends ChannelInitializer<SocketChannel> {

	/** http请求体的最大值(默认) */
	protected static final int DEFAULT_MAX_CONTENT_LENGTH = 20480;

	/** 管道中处理器组 */
	protected List<ChannelHandler> requestHandlers = new ArrayList<ChannelHandler>();

	/** 允许http请求体最大值 */
	protected int maxContentLength = DEFAULT_MAX_CONTENT_LENGTH;

	/** 指定工作线程组 */
	protected EventExecutorGroup eventExecutorGroup = null;

	/** https设置相关 */
	protected SslContext sslContext = null;

	/**
	 * 默认构造函数
	 */
	public PipelineInitializer() {
		super();
	}

	/**
	 * 为netty通道增加处理器
	 *
	 * @param handler
	 *            处理器
	 * @return 通道本身
	 */
	public PipelineInitializer addRequestHandler(ChannelHandler handler) {
		if (!requestHandlers.contains(handler)) {
			requestHandlers.add(handler);
		}

		return this;
	}

	/**
	 * 设置工作线程组
	 *
	 * @param executorGroup
	 *            工作线程组
	 * @return 通道本身
	 */
	public PipelineInitializer setExecutionHandler(EventExecutorGroup executorGroup) {
		this.eventExecutorGroup = executorGroup;
		return this;
	}

	/**
	 * Set the maximum length of the aggregated (chunked) content. If the length
	 * of the aggregated content exceeds this value, a TooLongFrameException
	 * will be raised during the request, which can be mapped in the HttpServer
	 * server to return a BadRequestException, if desired.
	 *
	 * @param value
	 *            允许http请求体最大值
	 * @return this PipelineBuilder for method chaining.
	 */
	public PipelineInitializer setMaxContentLength(int value) {
		this.maxContentLength = value;
		return this;
	}

	/**
	 * 设置https配置相关容器
	 *
	 * @param sslContext
	 *            https配置相关容器
	 * @return 通道本身
	 */
	public PipelineInitializer setSslContext(SslContext sslContext) {
		this.sslContext = sslContext;
		return this;
	}

	/**
	 * 取得https配置相关容器
	 *
	 * @return https配置相关容器
	 */
	public SslContext getSslContext() {
		return sslContext;
	}

	/**
	 * 初始化通道
	 *
	 * @param ch
	 *            通道类型
	 * @throws Exception
	 *             异常
	 */
	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		// 添加ssl处理器
		if (null != sslContext) {
			pipeline.addLast("ssl", sslContext.newHandler(ch.alloc()));
		}

		// 添加自己必须的处理器
		addOwnHandlers(pipeline);

		// 聚合器，把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse
		pipeline.addLast("aggregator", new HttpObjectAggregator(maxContentLength));

		// 块写入处理器
		pipeline.addLast("http-chunked", new ChunkedWriteHandler());

		addAllHandlers(pipeline);
	}

	/**
	 * 将必须的处理器加入到通道中
	 *
	 * @param pipeline
	 *            指定通道
	 */
	protected abstract void addOwnHandlers(ChannelPipeline pipeline);

	/**
	 * 将自定义处理器添加到指定通道中
	 *
	 * @param pipeline
	 *            指定通道
	 */
	protected void addAllHandlers(ChannelPipeline pipeline) {
		if (eventExecutorGroup != null) {
			for (ChannelHandler handler : requestHandlers) {
				pipeline.addLast(eventExecutorGroup, handler.getClass().getSimpleName(), handler);
			}
		} else {
			for (ChannelHandler handler : requestHandlers) {
				pipeline.addLast(handler.getClass().getSimpleName(), handler);
			}
		}
	}
}

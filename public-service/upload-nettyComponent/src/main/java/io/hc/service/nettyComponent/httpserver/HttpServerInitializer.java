package io.hc.service.nettyComponent.httpserver;

import io.hc.service.nettyComponent.common.PipelineInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * netty http server 进出管道初始化
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
public class HttpServerInitializer extends PipelineInitializer {

	/**
	 * 默认构造函数
	 */
	public HttpServerInitializer() {
		super();
	}

	@Override
	protected void addOwnHandlers(ChannelPipeline pipeline) {

		// Inbound handlers
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("inflater", new HttpContentDecompressor());

		// Outbound handlers
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("chunkWriter", new ChunkedWriteHandler());
		pipeline.addLast("deflater", new HttpContentCompressor());

	}

}

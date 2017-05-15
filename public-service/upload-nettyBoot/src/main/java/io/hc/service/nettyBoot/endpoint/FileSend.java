package io.hc.service.nettyBoot.endpoint;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.activation.MimetypesFileTypeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import io.hc.service.nettyBoot.cfg.NettyProperties;
import io.hc.service.nettyBoot.domain.common.HandleResult;
import io.hc.service.nettyBoot.utils.NettyResult;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;

/**
 * 下载文件
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
@Component
@EnableConfigurationProperties({ NettyProperties.class })
public class FileSend extends FileBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSend.class);

	@Autowired
	public FileSend(NettyProperties properties) {
		super(properties.getTempDir());
	}

	/**
	 * 推送文件给请求来源
	 *
	 * @param ctx
	 *            netty通道
	 * @param request
	 *            接收到信息
	 */
	public HandleResult sendFile(ChannelHandlerContext ctx, HttpRequest request) throws IOException {

		final String uri = request.getUri();
		final String path = sanitizeUri(uri);

		// 路径为空
		if (path == null) {
			return NettyResult.errorHandleResult(FORBIDDEN, "该端点必须要有文件名作为路径.");
		}

		// 文件不存在
		File file = new File(path);
		if (file.isHidden() || !file.exists()) {
			return NettyResult.errorHandleResult(NOT_FOUND, "文件不存在.");
		}

		// 不为文件
		if (!file.isFile()) {
			return NettyResult.errorHandleResult(FORBIDDEN, "只允许取得文件.");
		}

		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException ignore) {
			return NettyResult.errorHandleResult(NOT_FOUND, "文件不存在.");
		}
		long fileLength = raf.length();

		// 返回体设置
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		HttpHeaders.setContentLength(response, fileLength);
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));

		// 长链接
		if (HttpHeaders.isKeepAlive(request)) {
			response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		// Write the initial line and the header.
		ctx.write(response);

		// Write the content.
		ChannelFuture sendFileFuture;
		ChannelFuture lastContentFuture;
		if (ctx.pipeline().get(SslHandler.class) == null) {
			sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength),
					ctx.newProgressivePromise());
			// Write the end marker.
			lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		} else {
			sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
					ctx.newProgressivePromise());
			// HttpChunkedInput will write the end marker (LastHttpContent) for
			// us.
			lastContentFuture = sendFileFuture;
		}

		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
				if (total < 0) { // total unknown
					LOGGER.info("{} Transfer progress: {}", future.channel(), progress);
				} else {
					LOGGER.info("{} Transfer progress: {} / {}", future.channel(), progress, total);
				}
			}

			@Override
			public void operationComplete(ChannelProgressiveFuture future) {
				LOGGER.info("{} Transfer complete.", future.channel());
			}
		});

		// Decide whether to close the connection or not.
		if (!HttpHeaders.isKeepAlive(request)) {
			// Close the connection when the whole content is written out.
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}

		return null;
	}

}

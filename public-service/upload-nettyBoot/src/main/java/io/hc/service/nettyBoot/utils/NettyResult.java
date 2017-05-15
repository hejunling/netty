package io.hc.service.nettyBoot.utils;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.MediaType;
import com.google.gson.Gson;

import io.hc.service.nettyBoot.domain.common.Errors;
import io.hc.service.nettyBoot.domain.common.HandleResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

/**
 * 输出结果
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
public class NettyResult {

	private static final Logger LOGGER = LoggerFactory.getLogger(NettyResult.class);
	
	private static final Gson GSON = new Gson();

	/**
	 * 将结果写到通道中反馈回去
	 *
	 * @param ctx
	 *            netty 操作容器
	 * @param httpResponseStatus
	 *            http 状态
	 * @param returnMsg
	 *            返回信息
	 */
	public static ChannelFuture writeResponse(ChannelHandlerContext ctx, HttpResponseStatus httpResponseStatus,
			String returnMsg) {

		// 将请求响应的内容转换成ChannelBuffer.e
		ByteBuf buf = Unpooled.copiedBuffer(returnMsg, CharsetUtil.UTF_8);

		// 构建请求响应对象
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, httpResponseStatus);
		response.headers().set(CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
		// 若该请求响应是最后的响应，则在响应头中没有必要添加'Content-Length'
		response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes());

		response.content().writeBytes(buf);
		buf.release();

		LOGGER.info("请求结果为：{}", returnMsg);

		// Close the connection as soon as the error message is sent.
		return ctx.writeAndFlush(response);
	}

	/**
	 * 将结果写到通道,并关闭通道
	 *
	 * @param ctx
	 *            netty 操作容器
	 * @param httpResponseStatus
	 *            http 状态
	 * @param returnMsg
	 *            返回信息
	 */
	public static ChannelFuture writeResponseAndClose(ChannelHandlerContext ctx, HttpResponseStatus httpResponseStatus,
			String returnMsg) {
		return writeResponse(ctx, httpResponseStatus, returnMsg).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 将结果写到通道,并关闭通道
	 *
	 * @param ctx
	 *            netty 操作容器
	 * @param result
	 *            操作结果
	 */
	public static ChannelFuture writeResponseAndClose(ChannelHandlerContext ctx, HandleResult result) {
		String msg = GSON.toJson(result.getContent());
		return writeResponseAndClose(ctx, result.getStatus(), msg);
	}

	/**
	 * 创建错误返回体
	 *
	 * @param httpResponseStatus
	 *            返回状态信息
	 * @param returnMsg
	 *            原因
	 * @return 错误返回体
	 */
	public static HandleResult<Errors> errorHandleResult(HttpResponseStatus httpResponseStatus, String returnMsg) {
		Errors error = new Errors();
		error.setError(httpResponseStatus.reasonPhrase());
		error.setMessage(returnMsg);
		error.setStatus(httpResponseStatus.code());
		return new HandleResult<Errors>(httpResponseStatus, error);
	}

}

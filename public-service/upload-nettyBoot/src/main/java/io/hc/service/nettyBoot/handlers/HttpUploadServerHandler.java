package io.hc.service.nettyBoot.handlers;

import java.net.InetSocketAddress;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import io.hc.service.nettyBoot.constant.MsgConstant;
import io.hc.service.nettyBoot.domain.common.HandleResult;
import io.hc.service.nettyBoot.endpoint.FileDelete;
import io.hc.service.nettyBoot.endpoint.FileReceive;
import io.hc.service.nettyBoot.endpoint.FileSend;
import io.hc.service.nettyBoot.iplimit.IpFilter;
import io.hc.service.nettyBoot.iplimit.IpLimitProperties;
import io.hc.service.nettyBoot.utils.NettyResult;
import io.hc.service.nettyBoot.utils.NoticeUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 接收上传到yun对象存储的逻辑处理类
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
@Component(value = "httpUploadServerHandler")
@ChannelHandler.Sharable
@EnableConfigurationProperties({ IpLimitProperties.class })
public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpUploadServerHandler.class);

	/** IP 过滤 */
	@Resource
	private IpFilter ipFilter;

	/** 通知组件 */
	@Resource
	private NoticeUtil noticeUtil;

	/** 文件接收端点 */
	@Resource
	private FileReceive fileReceive;

	/** 文件发送端点 */
	@Resource
	private FileSend fileSend;

	/** 文件删除 */
	@Resource
	private FileDelete fileDelete;

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

		// 判断IP
		InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
		if (!ipFilter.accept(ctx, inetSocketAddress)) {
			ipFilter.channelRejected(ctx, inetSocketAddress).addListener(ChannelFutureListener.CLOSE);
			return;
		}

		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;

			HandleResult result = null;

			// 如果是post请求
			if (request.getMethod() == HttpMethod.POST) {
				// 接收文件
				result = fileReceive.receiveFile(ctx, request);
			} else if (request.getMethod() == HttpMethod.GET) {
				// 发送传输
				result = fileSend.sendFile(ctx, request);
			} else if (request.getMethod() == HttpMethod.DELETE) {
				// 删除
				result = fileDelete.deleteFile(ctx, request);
			}

			if (result != null) {
				NettyResult.writeResponseAndClose(ctx, result);
			}

		}
	}

	/**
	 * 异常处理
	 *
	 * @param ctx
	 *            netty通道信息
	 * @param cause
	 *            异常信息
	 * @throws Exception
	 *             会有异常抛出
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

		HttpResponseStatus status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
		// 错误信息
		String message = String.format(MsgConstant.REQUEST_RECEIVE_EXCEPTION, ctx.channel().remoteAddress(),
				cause.getMessage());

		// 如果是文件过大的异常
		if (cause instanceof TooLongFrameException) {
			message += MsgConstant.FILE_RECEIVE_SIZE_OUTMAX;
			status = HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE;
		}

		// 发送邮件通知管理员
		noticeUtil.sendNotice(MsgConstant.RECEIVE_EXCEPTION_NOTICE_TITLE, message);

		// 发送回馈信息给客户端
		NettyResult.writeResponseAndClose(ctx, NettyResult.errorHandleResult(status, message));

		LOGGER.error(message, cause);
	}

}

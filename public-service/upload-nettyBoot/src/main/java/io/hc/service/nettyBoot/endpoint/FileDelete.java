package io.hc.service.nettyBoot.endpoint;

import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import io.hc.service.nettyBoot.cfg.NettyProperties;
import io.hc.service.nettyBoot.domain.common.HandleResult;
import io.hc.service.nettyBoot.utils.NettyResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 文件删除端点
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
@Component
@EnableConfigurationProperties({ NettyProperties.class })
public class FileDelete extends FileBase {

	@Autowired
	public FileDelete(NettyProperties properties) {
		super(properties.getTempDir());
	}

	/**
	 * 根据请求信息删除文件
	 *
	 * @param ctx
	 *            netty通道
	 * @param request
	 *            接收到信息
	 */
	public HandleResult deleteFile(ChannelHandlerContext ctx, HttpRequest request) throws IOException {

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

		// 删除文件
		file.delete();

		return new HandleResult<String>(OK, "删除成功.");

	}

}
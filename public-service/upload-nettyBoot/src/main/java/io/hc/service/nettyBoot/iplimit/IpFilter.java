package io.hc.service.nettyBoot.iplimit;

import java.net.InetSocketAddress;

import io.hc.service.nettyBoot.domain.common.Errors;
import io.hc.service.nettyBoot.domain.common.HandleResult;
import io.hc.service.nettyBoot.utils.NettyResult;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.RuleBasedIpFilter;

/**
 * Ip 过滤
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
public class IpFilter extends RuleBasedIpFilter {

	public IpFilter(IpFilterRule... rules) {
		super(rules);
	}

	/**
	 * 该请求来源地址是否符合规则
	 *
	 * @param ctx
	 *            通道信息
	 * @param remoteAddress
	 *            IP相关信息
	 * @return 判断结果
	 * @throws Exception
	 *             异常信息
	 */
	public boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
		return super.accept(ctx, remoteAddress);
	}

	/**
	 * 返回请求拒绝信息
	 *
	 * @return A {@link ChannelFuture} if you perform I/O operations, so that
	 *         the {@link Channel} can be closed once it completes. Null
	 *         otherwise.
	 */
	@SuppressWarnings("UnusedParameters")
	public ChannelFuture channelRejected(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {

		Errors error = new Errors();
		error.setError(HttpResponseStatus.FORBIDDEN.reasonPhrase());
		error.setMessage("[" + remoteAddress.getAddress().getHostAddress() + "] 不在允许访问的ip列表");
		error.setStatus(HttpResponseStatus.FORBIDDEN.code());

		return NettyResult.writeResponseAndClose(ctx, new HandleResult<Errors>(HttpResponseStatus.FORBIDDEN, error));
	}
}

package io.hc.service.nettyBoot.cfg;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.List;

import javax.annotation.Resource;
import javax.net.ssl.SSLException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import io.hc.service.nettyBoot.handlers.HttpUploadServerHandler;
import io.hc.service.nettyBoot.iplimit.IpFilter;
import io.hc.service.nettyBoot.iplimit.IpLimitProperties;
import io.hc.service.nettyBoot.iplimit.Ipv4FilterRule;
import io.hc.service.nettyComponent.httpserver.HttpServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.IpFilterRuleType;

/**
 * 上传组件服务配置信息
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
@Configuration
@EnableConfigurationProperties({ NettyProperties.class, IpLimitProperties.class })
public class NettyServer {

	@Resource
	NettyProperties properties;

	@Resource
	IpLimitProperties ipLimit;

	@Resource(name = "httpUploadServerHandler")
	private HttpUploadServerHandler handler;

	@Bean
	@ConditionalOnMissingBean(IpFilter.class)
	public IpFilter noIpFilter() {

		return new IpFilter(new Ipv4FilterRule[] {}) {
			@Override
			public boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
				return true;
			}
		};
	}

	@Bean
	@ConditionalOnProperty("ipLimit.enabled")
	public IpFilter basedIpFilter() {
		List<Ipv4FilterRule> rejects = FluentIterable.from(ipLimit.getRejects())
				.transform(changeIpFilterRule(IpFilterRuleType.REJECT)).toList();
		List<Ipv4FilterRule> accepts = FluentIterable.from(ipLimit.getAccepts())
				.transform(changeIpFilterRule(IpFilterRuleType.ACCEPT)).toList();

		Ipv4FilterRule[] rules = FluentIterable.from(Iterables.concat(rejects, accepts)).toArray(Ipv4FilterRule.class);

		return new IpFilter(rules);
	}

	@Bean(destroyMethod = "shutdown")
	public HttpServer restExpress() throws CertificateException, SSLException {

		HttpServer server = HttpServer.builder().setName(properties.getName())
				.setIoThreadCount(properties.getWorkerThreads()).setMaxContentSize((int) properties.getMaxContentSize())
				.setPort(properties.getPort());

		// if (ipLimit.isEnabled()) {
		// server.addRequestHandler(basedIpFilter());
		// }

		server.addRequestHandler(handler);

		return server;
	}

	/**
	 * 将ip字符串转换ip过滤规则
	 *
	 * @param ruleType
	 *            规则
	 * @return ip过滤规则
	 */
	private Function<String, Ipv4FilterRule> changeIpFilterRule(final IpFilterRuleType ruleType) {
		return new Function<String, Ipv4FilterRule>() {
			@Override
			public Ipv4FilterRule apply(String input) {
				return new Ipv4FilterRule(input, ruleType);
			}
		};
	}

}

package io.hc.service.nettyBoot.iplimit;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import java.net.InetSocketAddress;

import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;

/**
 * ip规则
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
public class Ipv4FilterRule implements IpFilterRule {

	/** ip地址 */
	private final String ip;

	/** 规则类型 */
	private final IpFilterRuleType ruleType;

	/** ip类型 */
	private final IpType type;

	public Ipv4FilterRule(String ip, IpFilterRuleType type) {
		this.ip = ip;
		this.ruleType = type;
		this.type = IpType.selectIpType(this.ip);
	}

	@Override
	public boolean matches(InetSocketAddress remoteAddress) {
		return this.type.matches(remoteAddress.getAddress().getHostAddress(), this.ip);
	}

	@Override
	public IpFilterRuleType ruleType() {
		return this.ruleType;
	}

	enum IpType {
		/** 普通ip 例如：{@code 192.168.0.1} */
		NORMAL {

			@Override
			boolean matches(String ip, String matchesIp) {
				return Objects.equal(ip, matchesIp);
			}
		},
		/** 一网段内范围ip 例如：{@code 192.168.0.1-20} */
		RANG_IN_NETWORK_SEGMENT {

			@Override
			boolean matches(String ip, String matchesIp) {

				if (!commonCompare(ip, matchesIp)) {
					return false;
				}

				String commonPrefix = Strings.commonPrefix(ip, matchesIp);
				if (Strings.isNullOrEmpty(commonPrefix)) {
					return false;
				}

				int lastSegMentForIp = Ints.tryParse(lastSegment(ip, commonPrefix));
				Range<Integer> range = ipRange(lastSegment(matchesIp, commonPrefix));

				return range.contains(lastSegMentForIp);
			}
		},
		/** 一网段内所有ip 例如：{@code 192.168.0.*} */
		ALL_IN_NETWORK_SEGMENT {

			@Override
			boolean matches(String ip, String matchesIp) {

				if (!commonCompare(ip, matchesIp)) {
					return false;
				}

				String commonPrefix = Strings.commonPrefix(ip, matchesIp);

				return !Strings.isNullOrEmpty(commonPrefix);
			}
		};

		abstract boolean matches(String ip, String matchesIp);

		static IpType selectIpType(String matchesIp) {

			if (matchesIp.contains("*")) {
				return ALL_IN_NETWORK_SEGMENT;
			} else if (matchesIp.contains("-")) {
				return RANG_IN_NETWORK_SEGMENT;
			} else {
				return NORMAL;
			}

		}
	}

	/**
	 * 两ip是否是为空
	 *
	 * @param ip
	 *            需要匹配的ip
	 * @param matchesIp
	 *            匹配IP
	 * @return 判断结果
	 */
	private static boolean commonCompare(String ip, String matchesIp) {

		if (Strings.isNullOrEmpty(ip)) {
			return false;
		}

		if (Strings.isNullOrEmpty(matchesIp)) {
			return false;
		}

		return true;
	}

	/**
	 * 取得ip最后一段地址
	 *
	 * @param ip
	 *            ip
	 * @param prefix
	 *            ip前三段
	 * @return ip最后一段
	 */
	private static String lastSegment(String ip, String prefix) {
		String lastSegment = ip.substring(prefix.length());
		return lastSegment;
	}

	/**
	 * ip范围构造
	 *
	 * @param ipRangeSegment
	 *            ip范围段
	 * @return ip范围
	 */
	private static Range<Integer> ipRange(String ipRangeSegment) {

		Iterable<String> ipStr = Splitter.on("-").omitEmptyStrings().trimResults().limit(2).split(ipRangeSegment);

		return Range.encloseAll(Iterables.transform(ipStr, new Function<String, Integer>() {
			@Override
			public Integer apply(String input) {
				return Ints.tryParse(input);
			}
		}));

	}
}

package io.hc.service.nettyBoot.iplimit;

import com.google.common.collect.Lists;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * IP 限制规则配置
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
@ConfigurationProperties("ipLimit")
public class IpLimitProperties {

	/** 是否启用 */
	private boolean enabled = false;

	/** 拒绝列表 */
	private List<String> rejects = Lists.newArrayList();

	/** 接受列表 */
	private List<String> accepts = Lists.newArrayList();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<String> getRejects() {
		return rejects;
	}

	public void setRejects(List<String> rejects) {
		this.rejects = rejects;
	}

	public List<String> getAccepts() {
		return accepts;
	}

	public void setAccepts(List<String> accepts) {
		this.accepts = accepts;
	}
}

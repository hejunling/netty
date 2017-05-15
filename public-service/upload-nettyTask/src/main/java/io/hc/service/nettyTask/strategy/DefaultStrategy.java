package io.hc.service.nettyTask.strategy;

import org.springframework.stereotype.Component;

/**
 * 默认策略
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component(value = "defaultStrategy")
public class DefaultStrategy implements Strategy {

	/**
	 * 取得条件SQL
	 *
	 * @return
	 */
	@Override
	public String getStrategy() {
		return "";
	}
}

package io.hc.service.nettyTask.strategy;

/**
 * 扫描未完成任务策略接口类
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public interface Strategy {
	/**
	 * 取得条件SQL
	 *
	 * @return
	 */
	String getStrategy();
}

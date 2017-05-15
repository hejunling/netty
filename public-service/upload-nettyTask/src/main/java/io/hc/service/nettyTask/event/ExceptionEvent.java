package io.hc.service.nettyTask.event;

/**
 * eventBus异常事件
 * 
 * @author hechuan
 *
 * @created 2017年4月12日
 *
 * @since UPLOAD-3.0.0
 */
public class ExceptionEvent {

	/** 异常实体 */
	private final Throwable exception;

	/**
	 * 构造函数
	 *
	 * @param exception
	 */
	public ExceptionEvent(Throwable exception) {
		this.exception = exception;
	}

	/**
	 * 取得异常实体
	 *
	 * @return 取得异常
	 */
	public Throwable getException() {
		return this.exception;
	}
}

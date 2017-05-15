package io.hc.service.nettyBoot.domain.common;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 处理结果
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
public class HandleResult<T> {

	/** 处理结果 */
	private HttpResponseStatus status;

	/** 处理结果信息 */
	private T content;

	public HandleResult() {
	}

	public HandleResult(HttpResponseStatus status, T message) {
		this.status = status;
		this.content = message;
	}

	public HttpResponseStatus getStatus() {
		return status;
	}

	public HandleResult setStatus(HttpResponseStatus status) {
		this.status = status;
		return this;
	}

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}
}

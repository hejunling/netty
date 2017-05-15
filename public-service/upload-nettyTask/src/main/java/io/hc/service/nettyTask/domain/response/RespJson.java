package io.hc.service.nettyTask.domain.response;

/**
 * Json响应体类
 * 
 * @author hechuan
 *
 * @created 2017年4月12日
 *
 * @since UPLOAD-3.0.0
 */
public class RespJson<T> {

	private RespBody<T> response;

	public RespJson(RespBody<T> response) {
		this.response = response;
	}

	public RespBody<T> getResponse() {
		return response;
	}

	public void setResponse(RespBody<T> response) {
		this.response = response;
	}
}

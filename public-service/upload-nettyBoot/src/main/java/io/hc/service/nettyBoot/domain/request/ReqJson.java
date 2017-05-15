package io.hc.service.nettyBoot.domain.request;

/**
 * Json请求体类
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
public class ReqJson<T> {

	/** 请求体 */
	private ReqBody<T> request;

	public ReqJson(ReqBody<T> request) {
		this.request = request;
	}

	public ReqBody<T> getRequest() {
		return request;
	}

	public void setRequest(ReqBody<T> request) {
		this.request = request;
	}
}

package io.hc.service.nettyTask.domain.request;

import javax.xml.bind.annotation.*;

/**
 * 请求体的Body
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
@XmlRootElement(name = "request")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ReqBody<T> {
	@XmlElement
	private ReqCommon common;
	@XmlAnyElement(lax = true)
	private T content;

	public ReqBody() {
	}

	public ReqBody(T content) {
		if (null == this.common) {
			this.common = new ReqCommon();
		}
		this.content = content;
	}

	public ReqBody(ReqCommon common, T content) {
		this.common = common;
		this.content = content;
	}

	public ReqCommon getCommon() {
		return common;
	}

	public void setCommon(ReqCommon common) {
		this.common = common;
	}

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}

}

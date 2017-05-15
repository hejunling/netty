package io.hc.service.nettyTask.domain.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * 请求体body中共同参数部分
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
@XmlAccessorType(value = XmlAccessType.PROPERTY)
public class ReqCommon {
	private String action;

	private String reqtime;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getReqtime() {
		return reqtime;
	}

	public void setReqtime(String reqtime) {
		this.reqtime = reqtime;
	}

}

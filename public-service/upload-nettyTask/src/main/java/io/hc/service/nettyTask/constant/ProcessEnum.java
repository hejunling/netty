package io.hc.service.nettyTask.constant;

/**
 * 处理状态枚举
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
public enum ProcessEnum {
	/** 未处理 */
	NOTPROCESS(0, "未处理"),
	/** 处理中 */
	PROCESSING(1, "处理中");

	private int num;

	private String text;

	ProcessEnum(int num, String text) {
		this.num = num;
		this.text = text;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}

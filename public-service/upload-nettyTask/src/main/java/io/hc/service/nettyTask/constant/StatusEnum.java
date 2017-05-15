package io.hc.service.nettyTask.constant;

/**
 * 上传状态枚举
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
public enum StatusEnum {

	/** 上传失败 */
	FAIL(100001, "上传失败"),
	/** 上传成功 */
	SUCCESS(100000, "上传成功");

	private int num;

	private String text;

	StatusEnum(int num, String text) {
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

	/**
	 * 根据数值取得一个枚举，如果没有则返回上传失败
	 *
	 * @param num
	 * @return
	 */
	public static StatusEnum getStatusEnum(int num) {
		for (StatusEnum s : StatusEnum.values()) {
			if (s.getNum() == num) {
				return s;
			}
		}

		return FAIL;
	}
}

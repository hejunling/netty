package io.hc.service.nettyTask.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常工具类
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class ExceptionUtil {

	/**
	 * 以字符串形式返回异常堆栈信息
	 * 
	 * @param e
	 * @return 异常堆栈信息字符串
	 */
	public static String getStackTrace(Throwable e) {
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer, true));

		return writer.toString();
	}
}

package io.hc.service.nettyTask.utils.task;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * 标记一个任务方法
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ METHOD, ANNOTATION_TYPE })
public @interface HandleTask {

	/** 任务类型 */
	int taskHandler() default 0;

	/** 任务说明 */
	String description() default "";
}

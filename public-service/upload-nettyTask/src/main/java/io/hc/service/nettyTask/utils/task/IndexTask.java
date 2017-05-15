package io.hc.service.nettyTask.utils.task;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.common.base.Preconditions;

/**
 * 在一个特殊的对象上包含一个需要执行的只有一个参数的任务方法
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class IndexTask {

	/** 提供任务方法的对象 */
	private final Object target;

	/** 需要执行的任务方法 */
	private final Method method;

	/** 任务描述 */
	private final String description;

	/**
	 * Creates a new IndexTask to wrap {@code method} on @{code target}.
	 * 创建一个IndexTask用@{code target}包裹{@code method}
	 *
	 * @param target
	 *            提供任务方法的对象
	 * @param method
	 *            需要执行的任务方法
	 * @param description
	 *            任务描述
	 */
	IndexTask(Object target, Method method, String description) {

		Preconditions.checkNotNull(target, "IndexTask target cannot be null.");
		Preconditions.checkNotNull(method, "IndexTask method cannot be null.");

		this.target = target;
		this.method = method;
		method.setAccessible(true);
		this.description = description;
	}

	/**
	 * 用{@code task}作为参数执行用{@code HandleTask}标记过的方法
	 *
	 * @param task
	 *            执行方法参数
	 * @throws InvocationTargetException
	 *             if the wrapped method throws any {@link Throwable} that is
	 *             not an {@link Error} ({@code Error} instances are propagated
	 *             as-is).
	 */
	public void handleTask(Object task) throws InvocationTargetException {
		checkNotNull(task);
		try {
			method.invoke(target, new Object[] { task });
		} catch (IllegalArgumentException e) {
			throw new Error("Method rejected target/argument: " + task, e);
		} catch (IllegalAccessException e) {
			throw new Error("Method became inaccessible: " + task, e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Error) {
				throw (Error) e.getCause();
			}
			throw e;
		}
	}

	@Override
	public String toString() {
		return "[wrapper " + method + ", description " + description + "]";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		return (PRIME + method.hashCode()) * PRIME + System.identityHashCode(target);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IndexTask) {
			IndexTask that = (IndexTask) obj;
			// Use == so that different equal instances will still receive
			// events.
			// We only guard against the case that the same object is registered
			// multiple times
			return target == that.target && method.equals(that.method);
		}
		return false;
	}

	public Object getHandler() {
		return target;
	}

	public Method getMethod() {
		return method;
	}

	public String getDescription() {
		return description;
	}
}

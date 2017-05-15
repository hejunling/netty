package io.hc.service.nettyTask.utils.task;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * 收集所有被{@link HandleTask}注解标记的任务类的一种扫描任务策略{@link TaskFindingStrategy}
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class AnnotatedTaskFinder implements TaskFindingStrategy {

	/**
	 * A thread-safe cache that contains the mapping from each class to all
	 * methods in that class and all super-classes, that are annotated with
	 * {@code @HandleTask}. The cache is shared across all instances of this
	 * class; this greatly improves performance if multiple TaskBus instances
	 * are created and objects of the same class are registered on all of them.
	 */
	private static final LoadingCache<Class<?>, ImmutableList<Method>> handlerMethodsCache = CacheBuilder.newBuilder()
			.weakKeys().build(new CacheLoader<Class<?>, ImmutableList<Method>>() {
				@Override
				public ImmutableList<Method> load(Class<?> concreteClass) throws Exception {
					return getAnnotatedMethodsInternal(concreteClass);
				}
			});

	/**
	 * {@link TaskFindingStrategy#findAllTasks(Object)}
	 *
	 * 该实现方法会找到所有被{@link HandleTask} 注解标记的任务类
	 */
	@Override
	public Map<Integer, IndexTask> findAllTasks(Object handler) {

		Map<Integer, IndexTask> methodsInHandler = Maps.newLinkedHashMap();
		Class<?> clazz = handler.getClass();
		for (Method method : getAnnotatedMethods(clazz)) {
			HandleTask handleTask = method.getAnnotation(HandleTask.class);
			int taskHandler = handleTask.taskHandler();
			IndexTask task = makeHandler(handler, method, handleTask.description());
			if (methodsInHandler.containsKey(taskHandler)) {
				throw new IllegalArgumentException("TaskHandler " + taskHandler + "已经存在！");
			}
			methodsInHandler.put(taskHandler, task);
		}
		return methodsInHandler;
	}

	/**
	 * 从缓存中取得clazz类中所有被{@link HandleTask}标记过的方法
	 *
	 * @param clazz
	 *            需要被检索的类
	 * @return clazz类里所有被{@link HandleTask}标记过的方法不可变列表
	 */
	private static ImmutableList<Method> getAnnotatedMethods(Class<?> clazz) {
		try {
			return handlerMethodsCache.getUnchecked(clazz);
		} catch (UncheckedExecutionException e) {
			throw Throwables.propagate(e.getCause());
		}
	}

	/**
	 * 取得clazz类中所有被{@link HandleTask}标记过的方法
	 *
	 * @param clazz
	 *            需要被检索的类
	 * @return clazz类里所有被{@link HandleTask}标记过的方法不可变列表
	 */
	private static ImmutableList<Method> getAnnotatedMethodsInternal(Class<?> clazz) {
		Set<? extends Class<?>> supers = TypeToken.of(clazz).getTypes().rawTypes();
		Map<MethodIdentifier, Method> identifiers = Maps.newHashMap();
		for (Class<?> superClazz : supers) {
			for (Method superClazzMethod : superClazz.getMethods()) {
				if (superClazzMethod.isAnnotationPresent(HandleTask.class) && !superClazzMethod.isBridge()) {
					Class<?>[] parameterTypes = superClazzMethod.getParameterTypes();
					if (parameterTypes.length != 1) {
						throw new IllegalArgumentException("Method " + superClazzMethod
								+ " has @HandleTask annotation, but requires " + parameterTypes.length
								+ " arguments.  Task handler methods must require a single argument.");
					}

					MethodIdentifier ident = new MethodIdentifier(superClazzMethod);
					if (!identifiers.containsKey(ident)) {
						identifiers.put(ident, superClazzMethod);
					}
				}
			}
		}
		return ImmutableList.copyOf(identifiers.values());
	}

	/**
	 * 创建一个{@link IndexTask}对象用于在{@code handler}上调用{@code method}。
	 *
	 * @param handler
	 *            object bearing the task method.
	 * @param method
	 *            the task method to wrap in an IndexTask.
	 * @param description
	 *            the description of the task method to wrap in an IndexTask.
	 * @return an IndexTask that will call {@code method} on {@code handler}
	 *         when invoked.
	 */
	private static IndexTask makeHandler(Object handler, Method method, String description) {
		return new IndexTask(handler, method, description);
	}

	/**
	 * 自定义方法实体
	 */
	private static final class MethodIdentifier {
		private final String name;
		private final List<Class<?>> parameterTypes;

		MethodIdentifier(Method method) {
			this.name = method.getName();
			this.parameterTypes = Arrays.asList(method.getParameterTypes());
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(name, parameterTypes);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof MethodIdentifier) {
				MethodIdentifier ident = (MethodIdentifier) o;
				return name.equals(ident.name) && parameterTypes.equals(ident.parameterTypes);
			}
			return false;
		}
	}

}

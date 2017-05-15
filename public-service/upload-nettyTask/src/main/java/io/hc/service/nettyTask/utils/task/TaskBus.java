package io.hc.service.nettyTask.utils.task;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 任务总线
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class TaskBus {

	private static final Logger logger = LoggerFactory.getLogger(TaskBus.class);

	/** 重试任务所属线程池索引 */
	private static final Integer RETRY_TASK_POOL_INDEX = -1;

	/**
	 * 线程组缓存
	 */
	private final LoadingCache<Integer, ScheduledExecutorService> handlerThreadPoolCache;

	/**
	 * 所有被注册的任务类型，table第一坐标是任务类型，第二坐标为任务说明，第三坐标为任务实体
	 *
	 * <p>
	 * This LinkedHashMap is NOT safe for concurrent use; all access should be
	 * made after acquiring a read or write lock via {@link #taskActinLock}.
	 */
	private final Map<Integer, IndexTask> handlersByIndex = Maps.newLinkedHashMap();

	/** 任务表操作锁 */
	private final ReadWriteLock taskActinLock = new ReentrantReadWriteLock();

	/** 从注册的类里找出所有需要处理的任务方法的一种策略。 */
	private final TaskFindingStrategy finder = new AnnotatedTaskFinder();

	/**
	 * 构建TaskBus对象，默认线程组中线程数为5
	 */
	public TaskBus() {
		this(5);
	}

	/**
	 * 构建TaskBus对象，初始化线程组缓存
	 *
	 * @param threadPoolSize
	 */
	public TaskBus(final int threadPoolSize) {

		// 参数验证
		Preconditions.checkNotNull(threadPoolSize, "TaskBus threadPoolSize cannot be null.");

		// 初始化缓存
		handlerThreadPoolCache = CacheBuilder.newBuilder().weakKeys().weakValues().recordStats()
				.build(new CacheLoader<Integer, ScheduledExecutorService>() {
					@Override
					public ScheduledExecutorService load(Integer index) {
						return Executors.newScheduledThreadPool(threadPoolSize);
					}
				});
	}

	/**
	 * Registers all HandleTask methods on {@code object} to receive events.
	 * HandleTask methods are selected and classified using this EventBus's
	 * {@link TaskFindingStrategy}; the default strategy is the
	 * {@link AnnotatedTaskFinder}.
	 *
	 * @param object
	 *            object whose HandleTask methods should be registered.
	 */
	public void register(Object object) {
		Map<Integer, IndexTask> methodsInHandler = finder.findAllTasks(object);
		taskActinLock.writeLock().lock();
		try {
			handlersByIndex.putAll(methodsInHandler);
		} finally {
			taskActinLock.writeLock().unlock();
		}
	}

	/**
	 * Unregisters all HandleTask methods on a registered {@code object}.
	 *
	 * @param object
	 *            object whose handleTask methods should be unregistered.
	 */
	public void unregister(Object object) {
		Map<Integer, IndexTask> methodsInHandler = finder.findAllTasks(object);
		for (Map.Entry<Integer, IndexTask> entry : methodsInHandler.entrySet()) {
			taskActinLock.writeLock().lock();
			try {
				handlersByIndex.remove(entry.getKey());
			} finally {
				taskActinLock.writeLock().unlock();
			}
		}
	}

	/**
	 * 执行相应的任务
	 *
	 * @param retryFlg
	 *            是否重试
	 * @param index
	 *            任务类型
	 * @param task
	 *            任务实体
	 * @param duration
	 *            延迟时间
	 * @param timeUnit
	 *            延迟时间单位
	 */
	public void handlerTask(boolean retryFlg, final Integer index, final Object task, final long duration,
			final TimeUnit timeUnit) {

		// 取得线程组
		ScheduledExecutorService executorService = handlerThreadPool(retryFlg, index);

		// 可以执行任务方法的对象
		final IndexTask wrapper = handlersByIndex(index);

		// 将任务加入到执行队列中
		executorService.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					wrapper.handleTask(task);
				} catch (InvocationTargetException e) {
					logger.error("任务执行出错, 任务信息[{}], 错误信息:{}", task, e);
				}
			}
		}, duration, timeUnit);
	}

	/**
	 * 根据index从线程组缓存中取出所对应的线程组
	 *
	 * @param retryFlg
	 *            是否重试
	 * @param index
	 *            缓存中的键
	 * @return 线程组
	 */
	ScheduledExecutorService handlerThreadPool(boolean retryFlg, Integer index) {
		try {
			Integer poolIndex = retryFlg ? RETRY_TASK_POOL_INDEX : index;
			return handlerThreadPoolCache.getUnchecked(poolIndex);
		} catch (UncheckedExecutionException e) {
			throw Throwables.propagate(e.getCause());
		}
	}

	/**
	 * 根据键取得可以执行任务方法的对象
	 *
	 * @param index
	 *            键
	 * @return 可以执行任务方法的对象
	 */
	IndexTask handlersByIndex(Integer index) {
		taskActinLock.readLock().lock();
		try {
			return handlersByIndex.get(index);
		} finally {
			taskActinLock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("handlerThreadPoolCache", handlerThreadPoolCache.asMap())
				.add("CacheStats of handlerThreadPoolCache", handlerThreadPoolCache.stats())
				.add("handlersByIndex", handlersByIndex).toString();
	}
}

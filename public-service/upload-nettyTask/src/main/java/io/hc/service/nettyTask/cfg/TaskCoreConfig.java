package io.hc.service.nettyTask.cfg;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.hc.service.nettyTask.listener.TaskListener;
import io.hc.service.nettyTask.utils.task.TaskBus;

/**
 * task-core共通配置类
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
@Configuration
@EnableConfigurationProperties({ TaskProperties.class })
public class TaskCoreConfig {

	private static final Logger logger = LoggerFactory.getLogger(TaskCoreConfig.class);

	/** 任务相关配置 */
	@Resource
	private TaskProperties properties;

	/**
	 * 注入guava事件总线
	 *
	 * @return
	 */
	@Bean
	public EventBus eventBus() {

		EventBus eventBus = new EventBus();

		// 将非法事件监听器注册进事件总线
		eventBus.register(new Object() {

			@Subscribe
			public void lister(DeadEvent event) {
				logger.error("[{}]接收到非法事件[{}]!", event.getSource().getClass(), event.getEvent());
			}

		});

		return eventBus;
	}

	/**
	 * 注入任务总线类
	 *
	 * @return 任务总线实体
	 */
	@Bean
	public TaskBus taskBus() {
		return new TaskBus(properties.getWorkerThreads());
	}

	/**
	 * 注入任务监听器
	 *
	 * @return 任务监听器
	 */
	@Bean
	public TaskListener taskListener() {
		return new TaskListener(eventBus(), taskBus(), properties.getDuration());
	}

	/**
	 * rest请求客户端
	 *
	 * @return rest请求客户端
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}

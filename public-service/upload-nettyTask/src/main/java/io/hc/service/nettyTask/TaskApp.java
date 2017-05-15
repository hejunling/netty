package io.hc.service.nettyTask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 任务模块启动
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
@SpringBootApplication
public class TaskApp {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(TaskApp.class);
		app.setWebEnvironment(false);
		app.run(args);
	}

}

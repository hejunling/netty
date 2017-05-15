package io.hc.service.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hc.service.test.service.IDemoService;

/**
 * demo controller
 * 
 * @author hechuan
 *
 * @created 2017年5月10日
 *
 * @since UPLOAD-2.0.0
 */
@RestController
public class DemoController {

	private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

	@Autowired
	private IDemoService demoService;

	/**
	 * 上传 upload DemoController
	 */
	@RequestMapping("/netty/upload")
	public void upload() {
		logger.info("upload begin....");
		demoService.upload();

		logger.info("upload end....");
	}

	/**
	 * 回调 callBack DemoController
	 */
	@RequestMapping("/netty/callback")
	public void callBack() {
		logger.info("callback begin....");

		demoService.callback();

		logger.info("callback end....");
	}
}

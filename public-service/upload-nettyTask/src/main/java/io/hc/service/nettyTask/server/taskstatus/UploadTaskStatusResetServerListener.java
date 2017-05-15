package io.hc.service.nettyTask.server.taskstatus;

import javax.annotation.Resource;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.Service;

import io.hc.service.nettyTask.constant.BussinessConstant;
import io.hc.service.nettyTask.constant.MsgConstant;
import io.hc.service.nettyTask.utils.NoticeUtil;

/**
 * 上传组件任务状态重置服务监听器
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Primary
@Component
public class UploadTaskStatusResetServerListener extends TaskStatusResetServerListener {

	/** 通知组件 */
	@Resource
	private NoticeUtil noticeUtil;

	/**
	 * 服务终止时
	 *
	 * @param from
	 *            服务状态枚举
	 */
	@Override
	public void terminated(Service.State from) {

		// 构建信息
		String message = String.format(MsgConstant.SERVER_STOP_INFO, BussinessConstant.LOCALHOST.getHostAddress(), serverName);

		// 通知管理员
		noticeUtil.sendNotice(MsgConstant.SERVER_EXCEPTION, message);

		logger.info(message);
	}

	/**
	 * 服务出现异常时
	 *
	 * @param from
	 *            服务状态枚举
	 * @param failure
	 *            具体异常
	 */
	@Override
	public void failed(Service.State from, Throwable failure) {

		// 构建信息
		String message = String.format(MsgConstant.SERVER_EXCEPTION_INFO, BussinessConstant.LOCALHOST.getHostAddress(), serverName,
				failure.getCause());

		// 通知管理员
		noticeUtil.sendNotice(MsgConstant.SERVER_EXCEPTION, message);

		logger.info(message);
	}
}

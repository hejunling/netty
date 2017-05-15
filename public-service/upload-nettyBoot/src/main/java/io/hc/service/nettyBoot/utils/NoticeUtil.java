package io.hc.service.nettyBoot.utils;

import java.util.Calendar;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import io.hc.service.nettyBoot.cfg.NoticeProperties;
import io.hc.service.nettyBoot.constant.BussinessConstant;
import io.hc.service.nettyBoot.domain.request.ReqBody;
import io.hc.service.nettyBoot.domain.request.ReqCommon;
import io.hc.service.nettyBoot.domain.request.ReqJson;
import io.hc.service.nettyComponent.httpclient.HttpClient;
import io.netty.util.CharsetUtil;

/**
 * 发送通知工具类。
 *
 * @Created on 2015-02-11
 * @author xys
 * @version 1.0
 * @Copyright:杭州安存网络科技有限公司 Copyright (c) 2015
 */
@Component
@EnableConfigurationProperties({ NoticeProperties.class })
public class NoticeUtil {

	public static final Logger LOGGER = LoggerFactory.getLogger(NoticeUtil.class);

	/** 一天时间毫秒数 */
	private static final long MILLIS_FOR_ONE_DAY = 24 * 60 * 60 * 1000;

	/** 一天开始时间毫秒数 */
	private static long dayStartTimeMillis = 0;

	/** 当次通知时间毫秒数 */
	private static long currentNoticeTimeMillis = 0;

	/** 通知次数 */
	private static long noticeCount = 0;

	/** 通知相关配置 */
	@Resource
	private NoticeProperties properties;

	/**
	 * 发送通知信息
	 *
	 * @param subject
	 *            邮件标题
	 * @param message
	 *            通知信息
	 */
	public void sendNotice(String subject, String message) {

		// 基础请求体内容
		Map<String, String> content = baseContent(subject, message);

		// 发送短信通知
		sendSMS(properties.getUrl(), content);

		// 发送邮件通知
		sendEMAIL(properties.getUrl(), content);
	}

	/**
	 * 构建基础请求体内容
	 *
	 * @param subject
	 *            标题
	 * @param message
	 *            通知信息
	 * @return 基础请求体内容
	 */
	private Map<String, String> baseContent(String subject, String message) {
		Map<String, String> content = Maps.newHashMap();

		String localIp = BussinessConstant.LOCALHOST.getHostAddress();

		String msg = "服务器IP[" + localIp + "]：<br/>" + message;

		content.put("subject", subject);
		content.put("message", msg);
		content.put("asyn", "true");

		return content;
	}

	/**
	 * 创建一个请求体
	 *
	 * @param action
	 *            接口名称
	 * @param content
	 *            请求内容
	 * @param <T>
	 *            泛型
	 * @return 请求体
	 */
	private <T> ReqJson<T> creatReqJson(String action, T content) {
		// 请求体头部
		ReqCommon common = new ReqCommon();
		common.setAction(action);
		common.setReqtime(String.valueOf(System.nanoTime()));

		ReqBody<T> reqBody = new ReqBody<T>(common, content);
		ReqJson<T> reqJson = new ReqJson<T>(reqBody);

		return reqJson;
	}

	/**
	 * 发送短信通知
	 *
	 * @param url
	 *            通知服务器地址
	 * @param basecontent
	 *            请求体基本信息
	 */
	private void sendSMS(String url, Map<String, String> basecontent) {

		// 设置今天开始时间毫秒数
		if (dayStartTimeMillis == 0) {
			// 取得今天0点0时0分时的毫秒数
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.MILLISECOND, 0);
			dayStartTimeMillis = cal.getTimeInMillis();
		}

		// 设置当次通知时间
		currentNoticeTimeMillis = System.currentTimeMillis();

		// 如果当次时间-第一次通知时间超过24小时(即一天)
		if ((currentNoticeTimeMillis - dayStartTimeMillis) > MILLIS_FOR_ONE_DAY) {
			// 通知次数清0
			noticeCount = 0;
			// 向后移一天
			dayStartTimeMillis += MILLIS_FOR_ONE_DAY;
		} else {
			// 通知次数+1
			noticeCount++;
		}

		// 一天最大允许短信通知数
		// 如果未达到一天中允许的上限则允许短信通知
		if (noticeCount < properties.getMaxSendTimes() && !Strings.isNullOrEmpty(properties.getPhones())) {

			// 请求体内容
			Map<String, String> smsContent = basecontent;
			smsContent.put("phoneNo", properties.getPhones());
			ReqJson<Map<String, String>> reqJson = creatReqJson("sms", smsContent);

			// 发送短信通知
			HttpClient.post(url, BussinessConstant.GSON.toJson(reqJson).getBytes(), CharsetUtil.UTF_8);
		}

	}

	/**
	 * 发送短信通知
	 *
	 * @param url
	 *            通知服务器地址
	 * @param basecontent
	 *            请求体基本信息
	 */
	private void sendEMAIL(String url, Map<String, String> basecontent) {

		// 请求体内容
		Map<String, String> emailContent = basecontent;
		emailContent.put("emailTo", properties.getEmails());
		ReqJson<Map<String, String>> reqJson = creatReqJson("email", emailContent);

		// 发送短信通知
		HttpClient.post(url, BussinessConstant.GSON.toJson(reqJson).getBytes(), CharsetUtil.UTF_8);

	}
}

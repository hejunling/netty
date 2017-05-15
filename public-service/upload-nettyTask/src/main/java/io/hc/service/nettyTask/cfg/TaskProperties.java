package io.hc.service.nettyTask.cfg;

import com.google.common.base.Strings;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

/**
 * 任务相关配置
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
@ConfigurationProperties(prefix = "task")
public class TaskProperties {

	/** 任务执行线程数 */
	private int workerThreads = 10;

	/** 每次调度最大记录数 */
	private int maxCount = 20;

	/** 重试次数 */
	private int retryTimes = 3;

	/** 每次重做时间间隔(ms)=重试第几次*每次间隔 */
	private long duration = 60000;

	/** 初次执行任务delay时间,单位为毫秒，默认值为0，代表首次加载任务时立即执行 */
	private long delayTime = 0;

	/** 监视进程启动间隔时间(单位：秒，一天) */
	private long monitorTime = 86400000;

	/** 扫描进程间隔时间，单位为ms，默认值为0，代表任务只执行一次 */
	private long scanTime = 1000;

	/** 允许任务处于处理中的最长时间(单位：秒，两天) */
	private long statusTime = 172800000;

	/** 文件保存临时目录 */
	private String tempDir = "";

	public int getWorkerThreads() {
		return workerThreads;
	}

	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(long delayTime) {
		this.delayTime = delayTime;
	}

	public long getMonitorTime() {
		return monitorTime;
	}

	public void setMonitorTime(long monitorTime) {
		this.monitorTime = monitorTime;
	}

	public long getScanTime() {
		return scanTime;
	}

	public void setScanTime(long scanTime) {
		this.scanTime = scanTime;
	}

	public long getStatusTime() {
		return statusTime;
	}

	public void setStatusTime(long statusTime) {
		this.statusTime = statusTime;
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
		if (!Strings.isNullOrEmpty(tempDir)) {
			File file = new File(tempDir);
			if (!file.exists()) {
				file.mkdirs();
			}
		}
	}
}

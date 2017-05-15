package io.hc.service.nettyTask.service;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.hc.service.nettyTask.cfg.TaskProperties;
import io.hc.service.nettyTask.dao.TaskDao;
import io.hc.service.nettyTask.entity.Task;
import io.hc.service.nettyTask.entity.TaskStatusInfo;

/**
 * 扫描未完成任务
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Service(value = "scanService")
@EnableConfigurationProperties({ TaskProperties.class })
public class ScanService {

	/** 任务相关配置 */
	@Resource
	private TaskProperties properties;

	@Resource(name = "taskDao")
	private TaskDao taskDao;

	/**
	 * 取得任务类型列表
	 *
	 * @return 任务类型列表
	 */
	public List<Integer> scanTaskHandler() {
		return taskDao.selectTaskHandlers();
	}

	/**
	 * 根据条件Sql取得任务列表
	 *
	 * @param conditionSql
	 * @return
	 */
	public List<Task> scanTask(String conditionSql, int taskHandler) {

		StringBuilder sql = new StringBuilder();
		sql.append(" where task_status = 0 ");
		sql.append(" and task_handler = ");
		sql.append(taskHandler);
		sql.append(" ");
		if (StringUtils.hasText(conditionSql)) {
			sql.append(conditionSql);
		}
		sql.append(" order by gmt_create ");
		sql.append(" limit ");
		sql.append(properties.getMaxCount());

		return taskDao.selectTasks(sql.toString());
	}

	/**
	 * 根据任务ID开始任务，即将任务的状态变为处理中
	 *
	 * @param taskId
	 * @param gmtHandle
	 * @return
	 */
	public int startTask(String taskId, Timestamp gmtHandle) {
		return taskDao.processing(taskId, gmtHandle);
	}

	/**
	 * 将过长时间未处理的任务重置
	 * 
	 * @param firstScanFlg
	 *            是不是第一次扫描
	 * @param conditionSql
	 * @return
	 */
	public int resetTask(boolean firstScanFlg, String conditionSql) {
		return taskDao.resetTask(firstScanFlg, properties.getStatusTime(), conditionSql);
	}

	/**
	 * 取得未完成任务数
	 *
	 * @return 未完成任务状态列表
	 */
	public List<TaskStatusInfo> notCompleteTaskCount() {
		return taskDao.notCompleteTaskCount();
	}

	/**
	 * 取得未开始任务数
	 * 
	 * @return 未开始任务状态列表
	 */
	public List<TaskStatusInfo> notStartTaskCount() {
		return taskDao.notStartTaskCount();
	}

	/**
	 * 取得处理中任务数
	 *
	 * @return 处理中任务状态列表
	 */
	public List<TaskStatusInfo> handlingTaskCount() {
		return taskDao.handlingTaskCount();
	}
}

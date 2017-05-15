package io.hc.service.nettyTask.dao;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.google.common.eventbus.EventBus;

import io.hc.service.nettyTask.constant.ProcessEnum;
import io.hc.service.nettyTask.constant.TaskHandleTimeEnum;
import io.hc.service.nettyTask.entity.Task;
import io.hc.service.nettyTask.entity.TaskResult;
import io.hc.service.nettyTask.entity.TaskStatusInfo;
import io.hc.service.nettyTask.entity.TaskStatusInfoResult;
import io.hc.service.nettyTask.event.InQueneEvent;

/**
 * 任务数据库操作
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
@Repository(value = "taskDao")
public class TaskDao {

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Resource
	private EventBus eventBus;

	/** 成功任务表名前缀 */
	private final String PRE_TABLE_NAME_SUCCESS_TASK = "task_success_history_";

	/** 失败任务表名前缀 */
	private final String PRE_TABLE_NAME_FAIL_TASK = "task_fail_history_";

	/** 日期只取年月 */
	private final String DATE_FOEMAT = "yyyyMM";

	/** 建表语句 */
	private final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS {0} (\n" + "  task_id varchar(255) NOT NULL,\n"
			+ "  req_url varchar(255) NULL DEFAULT NULL,\n" + "  rev_url varchar(255) NULL DEFAULT NULL,\n"
			+ "  gmt_create timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\n"
			+ "  gmt_handle timestamp NULL DEFAULT NULL,\n" + "  gmt_complete timestamp NULL DEFAULT NULL,\n"
			+ "  task_handler int(11) NOT NULL,\n" + "  task_status int(11) NOT NULL DEFAULT 0,\n"
			+ "  compute_num int(11) NOT NULL DEFAULT 0,\n" + "  task_params varchar(4000) DEFAULT NULL,\n"
			+ "  retry_count int(11) NOT NULL DEFAULT 0,\n" + "  retry_reason varchar(4000) DEFAULT NULL,\n"
			+ "  PRIMARY KEY (task_id)\n" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

	/** 插入语句 */
	private final String INSERT_SQL = "INSERT INTO {0} VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	/** 插入语句 */
	private final String TASK_COMPLETE_INSERT_SQL = "INSERT INTO {0} VALUES (?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?);";

	/**
	 * 根据指定前缀取得当月的表名
	 *
	 * @param preName
	 * @return
	 */
	private String getCurrentMonthTableName(String preName) {
		Date d = new Date();

		return this.getTableName(d, preName);
	}

	/**
	 * 根据日期和指定前缀取得表名
	 *
	 * @param date
	 * @param preName
	 * @return
	 */
	private String getTableName(Date date, String preName) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FOEMAT);
		String dateStr = sdf.format(date);
		String tableName = preName + dateStr;

		return tableName;
	}

	/**
	 * 任务执行结束
	 *
	 * @param task
	 * @param preName
	 * @return
	 */
	private int complete(Task task, String preName) {
		int count = 0;

		// 建表
		String tableName = this.getCurrentMonthTableName(preName);
		count += jdbcTemplate.update(MessageFormat.format(CREATE_TABLE_SQL, tableName));

		// 插入数据
		Object[] params = new Object[] { task.getTaskId(), task.getReqUrl(), task.getRevUrl(), task.getGmtCreate(),
				task.getGmtHandle(), task.getTaskHandler(), task.getTaskStatus(), task.getComputeNum(),
				task.getTaskParams(), task.getRetryCount(), task.getRetryReason() };

		// 将数据插入到成功表中
		count += jdbcTemplate.update(MessageFormat.format(TASK_COMPLETE_INSERT_SQL, tableName), params);

		// 删除任务表中数据
		String deleteSql = "delete from task where task_id = ?";
		Object[] delParams = new Object[] { task.getTaskId() };
		count += jdbcTemplate.update(deleteSql, delParams);

		return count;
	}

	/**
	 * 新增任务
	 *
	 * @param task
	 * @return
	 */
	public int addTask(Task task) {

		// 插入数据
		Object[] params = new Object[] { task.getTaskId(), task.getReqUrl(), task.getRevUrl(), task.getGmtCreate(),
				task.getGmtHandle(), task.getTaskHandler(), task.getTaskStatus(), task.getComputeNum(),
				task.getTaskParams(), task.getRetryCount(), task.getRetryReason() };

		// 将数据插入到成功表中
		int count = jdbcTemplate.update(MessageFormat.format(INSERT_SQL, "task"), params);

		// 如果是已添加任务就执行
		if (task.getHandleTimeEnum() == TaskHandleTimeEnum.IMMEDIATELY) {
			InQueneEvent inQueneEvent = new InQueneEvent(task);
			eventBus.post(inQueneEvent);
		}

		return count;
	}

	/**
	 * 根据条件取得任务列表
	 *
	 * @param conditionSql
	 * @return
	 */
	public List<Task> selectTasks(String conditionSql) {

		StringBuilder sb = new StringBuilder();
		sb.append(" select * from task ");
		if (StringUtils.hasText(conditionSql)) {
			sb.append(conditionSql);
		}

		return jdbcTemplate.query(sb.toString(), new TaskResult());
	}

	/**
	 * 将处于处理中状态过长时间的任务置为未处理
	 * 
	 * @param firstScanFlg
	 * @param delayTime
	 * @param conditionSql
	 * @return
	 */
	public int resetTask(boolean firstScanFlg, long delayTime, String conditionSql) {

		StringBuilder sb = new StringBuilder();
		sb.append(" update task set task_status = ");
		sb.append(ProcessEnum.NOTPROCESS.getNum());
		sb.append(" where task_status = ");
		sb.append(ProcessEnum.PROCESSING.getNum());
		// 如果是第一次扫描，则全部置回未处理状态
		if (!firstScanFlg) {
			sb.append(" and UNIX_TIMESTAMP() - UNIX_TIMESTAMP(gmt_handle)>= ");
			sb.append(delayTime);
		}
		sb.append(conditionSql);

		return jdbcTemplate.update(sb.toString());
	}

	/**
	 * 将过长时间未处理的任务重置
	 *
	 * @param taskId
	 * @param gmtHandle
	 * @return
	 */
	public int processing(String taskId, Timestamp gmtHandle) {

		StringBuilder sb = new StringBuilder();
		sb.append(" update task set task_status = ");
		sb.append(ProcessEnum.PROCESSING.getNum());
		sb.append(", gmt_handle = ? where task_id = ? and task_status = ");
		sb.append(ProcessEnum.NOTPROCESS.getNum());

		Object[] params = new Object[] { gmtHandle, taskId };

		return jdbcTemplate.update(sb.toString(), params);
	}

	/**
	 * 任务失败，重试
	 *
	 * @param task
	 *            任务
	 * @return 更新条数
	 */
	public int retry(Task task) {

		StringBuilder sb = new StringBuilder();
		sb.append(" update task set task_status = ");
		sb.append(ProcessEnum.NOTPROCESS.getNum());
		sb.append(", retry_count = retry_count + 1, retry_reason = ?");
		sb.append(", task_params = ?");
		sb.append(" where task_id = ?");

		Object[] params = new Object[] { task.getRetryReason(), task.getTaskParams(), task.getTaskId() };

		return jdbcTemplate.update(sb.toString(), params);
	}

	/**
	 * 任务执行成功
	 *
	 * @param task
	 * @return
	 */
	public int success(Task task) {
		return this.complete(task, PRE_TABLE_NAME_SUCCESS_TASK);
	}

	/**
	 * 任务执行失败
	 *
	 * @param task
	 * @return
	 */
	public int fail(Task task) {
		return this.complete(task, PRE_TABLE_NAME_FAIL_TASK);
	}

	/**
	 * 取得未完成任务数
	 *
	 * @return
	 */
	public List<TaskStatusInfo> notCompleteTaskCount() {
		StringBuilder sb = new StringBuilder();
		sb.append(" select compute_num, count(*) as task_num from task group by compute_num");

		return jdbcTemplate.query(sb.toString(), new TaskStatusInfoResult());
	}

	/**
	 * 取得未开始任务数
	 * 
	 * @return
	 */
	public List<TaskStatusInfo> notStartTaskCount() {
		StringBuilder sb = new StringBuilder();
		sb.append(" select compute_num, count(*) as task_num from task where task_status = 0 group by compute_num ");

		return jdbcTemplate.query(sb.toString(), new TaskStatusInfoResult());
	}

	/**
	 * 取得执行中任务数
	 *
	 * @return
	 */
	public List<TaskStatusInfo> handlingTaskCount() {
		StringBuilder sb = new StringBuilder();
		sb.append(" select compute_num, count(*) as task_num from task where task_status = 1 group by compute_num ");

		return jdbcTemplate.query(sb.toString(), new TaskStatusInfoResult());
	}

	/**
	 * 根据条件取得任务类型列表
	 *
	 * @return
	 */
	public List<Integer> selectTaskHandlers() {

		StringBuilder sb = new StringBuilder();
		sb.append(" select distinct(task_handler) from task ");

		return jdbcTemplate.queryForList(sb.toString(), Integer.class);
	}
}

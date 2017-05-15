package io.hc.service.nettyBoot.domain.task;

import java.text.MessageFormat;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 任务数据库操作
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
@Repository(value = "taskDao")
public class TaskDao {

	/** 表名 */
	private static final String TABLE_NAME = "task";

	@Resource
	private JdbcTemplate jdbcTemplate;

	/** 插入语句 */
	private final String INSERT_SQL = "INSERT INTO {0} VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

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
		int count = jdbcTemplate.update(MessageFormat.format(INSERT_SQL, TABLE_NAME), params);

		return count;
	}

}

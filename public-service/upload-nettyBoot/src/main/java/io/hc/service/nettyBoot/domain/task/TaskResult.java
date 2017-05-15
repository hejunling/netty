package io.hc.service.nettyBoot.domain.task;

import com.google.gson.Gson;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 任务持久化信息表数据映射
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
public class TaskResult implements RowMapper<Task> {
	@SuppressWarnings("unchecked")
	@Override
	public Task mapRow(ResultSet resultSet, int i) throws SQLException {
		Task task = new Task();
		// 将数据库自动生成的信息设置到用户对象中
		task.setTaskId(resultSet.getString("task_id"));
		task.setReqUrl(resultSet.getString("req_url"));
		task.setRevUrl(resultSet.getString("rev_url"));
		task.setGmtCreate(resultSet.getTimestamp("gmt_create"));
		task.setGmtHandle(resultSet.getTimestamp("gmt_handle"));
		task.setTaskHandler(resultSet.getInt("task_handler"));
		task.setTaskStatus(resultSet.getInt("task_status"));
		task.setTaskStatus(resultSet.getInt("task_status"));
		task.setComputeNum(resultSet.getInt("compute_num"));
		task.setTaskParams(resultSet.getString("task_params"));
		task.setRetryCount(resultSet.getInt("retry_count"));
		task.setRetryReason(resultSet.getString("retry_reason"));
		task.setParamsMap((new Gson()).fromJson(task.getTaskParams(), Map.class));

		return task;
	}
}

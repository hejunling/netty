package io.hc.service.nettyTask.entity;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 任务状态信息数据映射
 * 
 * @author hechuan
 *
 * @created 2017年4月12日
 *
 * @since UPLOAD-3.0.0
 */
public class TaskStatusInfoResult implements RowMapper<TaskStatusInfo> {
	@Override
	public TaskStatusInfo mapRow(ResultSet rs, int rowNum) throws SQLException {

		TaskStatusInfo statusInfo = new TaskStatusInfo();

		statusInfo.setComputeNum(rs.getInt("compute_num"));

		statusInfo.setTaskNum(rs.getLong("task_num"));

		return statusInfo;
	}
}

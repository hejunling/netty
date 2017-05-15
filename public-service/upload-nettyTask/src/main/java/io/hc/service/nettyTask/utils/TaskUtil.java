package io.hc.service.nettyTask.utils;

import java.util.List;
import java.util.Map;

import io.hc.service.nettyTask.entity.Task;

/**
 * 集合工具类
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class TaskUtil {

	/**
	 * 根据任务Id删除list中相应元素
	 *
	 * @param tasks
	 * @param taskId
	 */
	public static void removeTask(List<Task> tasks, String taskId) {
		for (Task task : tasks) {
			if (task.getTaskId().equals(taskId)) {
				tasks.remove(task);
			}
		}
	}

	/**
	 * 从map中取得值
	 *
	 * @param objectMap
	 * @param key
	 * @return
	 */
	public static String getValue(Map<String, Object> objectMap, String key) {
		String result = "";
		if (objectMap.get(key) != null) {
			result = objectMap.get(key).toString();
		}
		return result;
	}
}

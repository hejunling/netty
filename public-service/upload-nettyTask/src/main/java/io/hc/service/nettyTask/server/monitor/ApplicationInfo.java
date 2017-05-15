package io.hc.service.nettyTask.server.monitor;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;

import io.hc.service.nettyTask.constant.MsgConstant;
import io.hc.service.nettyTask.entity.TaskStatusInfo;
import io.hc.service.nettyTask.server.ServerManager;
import io.hc.service.nettyTask.service.ScanService;
import io.hc.service.nettyTask.utils.task.TaskBus;

/**
 * 应用信息
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component
public class ApplicationInfo {

	/** 换行符 */
	private static final String JOINER_STRING = "\n";

	/** 扫描service */
	@Resource
	private ScanService scanService;

	/** 任务总线 */
	@Resource
	private TaskBus taskBus;

	/** 服务管理 */
	@Resource
	private ServerManager manager;

	/** 自定义系统信息 */
	@Resource
	private CustomApplicationInfo customApplicationInfo;

	/**
	 * 获取监视信息
	 *
	 * @return 监视信息
	 */
	public String monitor() {

		// 自定义任务信息
		String customInfo = customApplicationInfo.supplyCustomApplicationInfo();

		// 未完成任务信息
		List<TaskStatusInfo> noCompletes = scanService.notCompleteTaskCount();

		// String noCompletesMessage =
		// SpringContextUtil.getMessage(MsgConstant.NOCOMPLETE_TASK_COUNT,
		// new Object[]{ noCompletes.toString() });
		String noCompletesMessage = String.format(MsgConstant.NOCOMPLETE_TASK_COUNT, noCompletes.toString());

		// 未开始任务信息
		List<TaskStatusInfo> noStarts = scanService.notStartTaskCount();
		// String noStartsMessage =
		// SpringContextUtil.getMessage(MsgConstant.NOSTART_TASK_COUNT,
		// new Object[]{noStarts.toString()});
		String noStartsMessage = String.format(MsgConstant.NOSTART_TASK_COUNT, noStarts.toString());

		// 执行中任务信息
		List<TaskStatusInfo> handlings = scanService.handlingTaskCount();
		// String handlingsMessage =
		// SpringContextUtil.getMessage(MsgConstant.NOSTART_TASK_COUNT,
		// new Object[]{handlings.toString()});
		String handlingsMessage = String.format(MsgConstant.HANDLING_TASK_COUNT, handlings);

		return Joiner.on(JOINER_STRING).skipNulls().join(customInfo, noCompletesMessage, noStartsMessage,
				handlingsMessage, manager.toString(), taskBus.toString());
	}

}

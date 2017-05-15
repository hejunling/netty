package io.hc.service.nettyBoot.constant;

/**
 * 消息相关
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
public interface MsgConstant {

	/** 请求异常 */
	String REQUEST_RECEIVE_EXCEPTION = "从节点[%s]接收到文件过程中出现异常：%s。";

	/** 请求体太大 */
	String FILE_RECEIVE_SIZE_OUTMAX = "异常发生原因：文件超过服务器设置的所允许的最大大小！";

	/** 数据接收异常报警标题 */
	String RECEIVE_EXCEPTION_NOTICE_TITLE = "上传组件接收数据发生异常";

	/** 服务器节点信息 */
	String SERVER_NODE_INFO = "服务器节点[%s]";

	/** 文件接收成功信息 */
	String FILE_RECEIVE_SUCCESS = "已从节点[%s]接收到文件[%s],准备上传到云对象存储。";

	/** 上传组件异常 */
	String SERVER_EXCEPTION = "上传组件异常";

	/** 服务异常信息 */
	String SERVER_EXCEPTION_INFO = "服务器节点[%s]上,[%s]服务发生异常，cause：%s";

}
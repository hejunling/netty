package io.hc.service.nettyTask.constant;

/**
 * 常量类
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
public class MsgConstant {

    /** 未完成任务数 */
    public static final String NOCOMPLETE_TASK_COUNT = "未完成任务信息：%s";

    /** 未开始任务数 */
    public static final String NOSTART_TASK_COUNT = "未开始任务信息：%s";

    /** 执行中任务数 */
    public static final String HANDLING_TASK_COUNT = "执行中任务信息：%s";

    /** 上传组件日常监控 */
    public static final String UPLOAD_MONITOR = "上传组件日常监控";

    /** 上传组件异常 */
    public static final String SERVER_EXCEPTION = "上传组件异常";

    /** 服务停止信息 */
    public static final String SERVER_STOP_INFO = "服务器节点[%s]上,[%s]服务停止";

    /** 服务异常信息 */
    public static final String SERVER_EXCEPTION_INFO = "服务器节点[%s]上,[%s]服务发生异常，cause：%s";

    /** 文件上传失败 */
    public static final String FILE_UPLOAD_FAILURE = "文件[%s]在服务器节点[%s]上%s，上传到云的信息[%s]，已重试3次还是失败，失败原因【%s】。";

    /** 上传功能异常报警标题 */
    public static final String UPLOAD_EXCEPTION_NOTICE_TITLE = "上传组件上传功能发生异常";

    /** 回调异常情况1 */
    public static final String SERVER_CALLBACK_RETRY_REASON_1 = "向服务器[%s]发送回调请求时出现异常，回调服务器基础信息[%s]，响应结果为%s。";

    /** 回调异常情况2 */
    public static final String SERVER_CALLBACK_RETRY_REASON_2 = "向服务器[%s]发送回调请求时出现异常，回调服务器基础信息[%s]。";

    /** 回调成功 */
    public static final String SERVER_CALLBACK_SUCCESS = "文件[%s]在服务器节点[%s]上处理结束，并且向服务器[%s]发送回调请求成功！";

    /** 回调失败 */
    public static final String SERVER_CALLBACK_FAILURE = "文件[%s]在服务器节点[%s]上回调服务器[%s]发送回调请求不成功，回调服务器基础信息[%s]，已重试3次还是失败，失败原因【%s】。";

    /** 回调异常异常报警标题 */
    public static final String CALLBACK_EXCEPTION_NOTICE_TITLE = "上传组件回调功能发生异常";
    
    /** 请求异常 */
    public static final String REQUEST_RECEIVE_EXCEPTION = "从节点[%s]接收到文件过程中出现异常：%s。";

    /** 请求体太大 */
    public static final String FILE_RECEIVE_SIZE_OUTMAX = "异常发生原因：文件超过服务器设置的所允许的最大大小！";

    /** 数据接收异常报警标题 */
    public static final String RECEIVE_EXCEPTION_NOTICE_TITLE = "上传组件接收数据发生异常";

    /** 服务器节点信息 */
    public static final String SERVER_NODE_INFO = "服务器节点[%s]";

    /** 文件接收成功信息 */
    public static final String FILE_RECEIVE_SUCCESS = "已从节点[%s]接收到文件[%s],准备上传到云对象存储。";
}

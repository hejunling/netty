package io.hc.service.nettyTask.constant;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;
import com.google.gson.Gson;

/**
 * 业务常量类
 * 
 * @author hechuan
 *
 * @created 2017年4月11日
 *
 * @since UPLOAD-3.0.0
 */
public class BussinessConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(BussinessConstant.class);
    
    /** 用户meta信息前缀 */
    public static final String USER_META_INFO_PREFIX = "x-oss-meta-";

    /** 上传OSS时显示文件名 */
    public static final String FILE_KEY = "fileKey";

    /** 文件路径标记 */
    public static final String FILE_NAME = "file_name";
    
    /** 文件网路路径 */
    public static final String FILE_URL = "file_url";

    /**  回调参数名 */
    public static final String CALLBACK_URI = "callbackUri";

    /**  文件md5参数名 */
    public static final String FILE_MD5 = "fileMd5";

    /** 上传云类型参数名 */
    public static final String YUN_TYPE = "yunType";

    /**  时间格式 */
    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** 将文件上传到云 */
    public static final int UPTOYUN = 0;
    
    /** 任务为处理 */
    public static final int UN_PROCESS = 0;

    /** 将上传结果通知回调方 */
    public static final int CALLBACK = 1;

    /** 成功 */
    public static final int SUCCESS = 100000;
    
    /** gson工具类 */
    public static final Gson GSON = new Gson();

    /** 本地IP地址 */
    public static final InetAddress LOCALHOST;

    static {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LOGGER.warn("error then get host info", e);
            localhost = InetAddresses.forString("127.0.0.1");
        }
        LOCALHOST = localhost;
    }

}

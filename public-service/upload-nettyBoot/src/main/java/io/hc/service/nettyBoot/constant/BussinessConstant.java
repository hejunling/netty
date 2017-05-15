package io.hc.service.nettyBoot.constant;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.netty.util.NetUtil;

/**
 * 业务常量类
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
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

	/** 文件网络路径标记 */
	public static final String FILE_URL = "file_url";

	/** 文件md5参数名 */
	public static final String FILE_MD5 = "fileMd5";

	/** 将文件上传到云 */
	public static final int UPTOYUN = 0;

	/** 任务为处理 */
	public static final int UN_PROCESS = 0;

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
			localhost = NetUtil.LOCALHOST4;
		}
		LOCALHOST = localhost;
	}

}

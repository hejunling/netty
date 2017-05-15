package io.hc.service.nettyBoot.endpoint;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.net.HostAndPort;

import io.hc.service.nettyBoot.cfg.NettyProperties;
import io.hc.service.nettyBoot.constant.BussinessConstant;
import io.hc.service.nettyBoot.constant.MsgConstant;
import io.hc.service.nettyBoot.domain.common.HandleResult;
import io.hc.service.nettyBoot.domain.task.Task;
import io.hc.service.nettyBoot.domain.task.TaskDao;
import io.hc.service.nettyBoot.handlers.HttpUploadServerHandler;
import io.hc.service.nettyBoot.utils.NettyResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

/**
 * 接收文件，并添加到任务
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
@Component
@Scope("prototype")
@EnableConfigurationProperties({ NettyProperties.class })
public class FileReceive {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpUploadServerHandler.class);

	/** 文件路径标记 */
	private static final String FILE_PATH = "file_path";

	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

	private ThreadLocal<HttpRequest> requestLocal = new ThreadLocal<HttpRequest>();

	private ThreadLocal<HttpPostRequestDecoder> decoderLocal = new ThreadLocal<HttpPostRequestDecoder>();

	/** 临时文件夹 */
	private final String tempDir;

	/** 任务操作Dao */
	private final TaskDao taskDao;

	/** 该端点请求地址 */
	private final String LOCALURI;

	/** 执行任务所需的参数 */
	private ThreadLocal<Map<String, Object>> taskParamsLocal = new ThreadLocal<Map<String, Object>>();

	// 开始执行时间
	private volatile long beginTime = 0;

	@Autowired
	public FileReceive(NettyProperties properties, TaskDao taskDao) {

		this.tempDir = properties.getTempDir();

		this.taskDao = taskDao;

		this.LOCALURI = Joiner.on("").join("http://",
				HostAndPort.fromParts(BussinessConstant.LOCALHOST.getHostAddress(), properties.getPort()), "/");

	}

	/**
	 * 接收文件,并添加到任务
	 *
	 * @param ctx
	 *            通道信息
	 * @param msg
	 *            接收到的数据
	 */
	public HandleResult receiveFile(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {

		Map<String, Object> taskParams = taskParamsLocal.get();
		if (taskParams == null) {
			taskParams = Maps.newHashMap();
			taskParamsLocal.set(taskParams);
		}

		try {
			// 开始接收文件
			beginTime = System.currentTimeMillis();
			LOGGER.info("接收文件请求读取开始：{}", new Object[] { beginTime });

			if (requestLocal.get() == null) {
				requestLocal.set(msg);
			}

			HttpRequest request = requestLocal.get();

			// 设置用户meta信息
			Map<String, String> metadata = new HashMap<String, String>();
			for (Map.Entry<String, String> entry : request.headers()) {
				if (entry.getKey().startsWith(BussinessConstant.USER_META_INFO_PREFIX)) {
					metadata.put(entry.getKey(), entry.getValue());
				}
			}

			taskParams.put("user_meta_info", metadata);

			if (request.getMethod() == HttpMethod.POST) {
				if (decoderLocal.get() != null) {
					decoderLocal.get().cleanFiles();
					decoderLocal.remove();
				}
				try {
					if (decoderLocal.get() == null) {
						decoderLocal.set(new HttpPostRequestDecoder(factory, request));
					}
				} catch (Exception e) {
					LOGGER.error("解析请求体出现异常：", e);
					return NettyResult.errorHandleResult(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
				}
			}

			HttpPostRequestDecoder decoder = decoderLocal.get();

			if (decoder != null && msg instanceof HttpContent) {
				HttpContent chunk = (HttpContent) msg;

				try {
					decoder.offer(chunk);
				} catch (Exception e) {
					LOGGER.info("请求体解析异常", e);
					return NettyResult.errorHandleResult(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
				}

				readHttpDataChunkByChunk();

				if (chunk instanceof LastHttpContent) {

					// 文件完整性校验
					String checkResult = fileIntegrityCheck(ctx, taskParams);
					if (!Strings.isNullOrEmpty(checkResult)) {
						HttpResponseStatus status = new HttpResponseStatus(10001, checkResult);
						return NettyResult.errorHandleResult(HttpResponseStatus.BAD_REQUEST, checkResult);
					}

					// 如果上传到OSS的Key为空则用文件名
					if (taskParams.get(BussinessConstant.FILE_KEY) == null
							|| "".equals(taskParams.get(BussinessConstant.FILE_KEY).toString())) {
						taskParams.put(BussinessConstant.FILE_KEY,
								taskParams.get(BussinessConstant.FILE_NAME).toString());
					}

					// 开始持久化数据(将任务持久化到数据库)
					long beginSaveTaskTime = System.currentTimeMillis();

					// 持久化信息
					Task task = new Task();
					task.setTaskId(String.valueOf(UUID.randomUUID()));
					task.setReqUrl(ctx.channel().remoteAddress().toString());
					task.setRevUrl(BussinessConstant.LOCALHOST.getHostAddress());
					task.setTaskParams(BussinessConstant.GSON.toJson(taskParams));
					task.setParamsMap(taskParams);
					task.setComputeNum(0);
					task.setTaskHandler(BussinessConstant.UPTOYUN);
					task.setTaskStatus(BussinessConstant.UN_PROCESS);
					task.setGmtCreate(new Timestamp(System.currentTimeMillis()));
					task.setGmtHandle(new Timestamp(System.currentTimeMillis()));
					taskDao.addTask(task);

					// 接收文件请求结束
					long endSaveTaskTime = System.currentTimeMillis();
					LOGGER.info("文件 ：[{}] 持久化数据(将任务持久化到数据库)总共花费时间：{}ms", new Object[] {
							taskParams.get(BussinessConstant.FILE_KEY), (endSaveTaskTime - beginSaveTaskTime) });

					// 反馈消息给客户端
					long endResponseTime = System.currentTimeMillis();
					LOGGER.info("文件 ：[{}] 反馈消息给客户端总共花费时间：{}ms",
							new Object[] { taskParams.get(BussinessConstant.FILE_KEY), (endResponseTime - beginTime) });

					// 组建正常信息
					String okMsg = String.format(MsgConstant.SERVER_NODE_INFO + MsgConstant.FILE_RECEIVE_SUCCESS,
							ctx.channel().localAddress().toString(), ctx.channel().remoteAddress().toString(),
							taskParams.get(BussinessConstant.FILE_KEY).toString());

					reset();

					return new HandleResult<String>(HttpResponseStatus.OK, okMsg);
				}
			}
		} catch (Exception e) {
			File file = new File(taskParams.get(FILE_PATH).toString());
			if (file.exists()) {
				file.delete();
			}
		}
		return null;
	}

	/**
	 * 重置属性
	 */
	private void reset() {
		requestLocal.remove();

		// 销毁decoder释放所有的资源
		decoderLocal.get().destroy();
		decoderLocal.remove();

		taskParamsLocal.remove();
	}

	/**
	 * 通过chunk读取request，获取chunk数据
	 * 
	 * @throws java.io.IOException
	 */
	private void readHttpDataChunkByChunk() throws IOException {
		try {
			while (decoderLocal.get().hasNext()) {

				InterfaceHttpData data = decoderLocal.get().next();
				if (data != null) {
					try {
						writeHttpData(data);
					} finally {
						data.release();
					}
				}
			}
		} catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
			// chunk读取结束正常操作，不做任何处理
			// System.out.println("end chunk");
		}
	}

	/**
	 * 根据取得chunk数据类型进行相应操作
	 * 
	 * @param data
	 *            chunk数据
	 * @throws java.io.IOException
	 */
	private void writeHttpData(InterfaceHttpData data) throws IOException {
		Map<String, Object> taskParams = taskParamsLocal.get();
		if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
			FileUpload fileUpload = (FileUpload) data;
			if (fileUpload.isCompleted()) {

				taskParams.put(BussinessConstant.FILE_NAME, fileUpload.getFilename());

				beginTime = System.currentTimeMillis();
				LOGGER.info("文件 ：[{}] 接收开始：{}", fileUpload.getFilename(), beginTime);

				StringBuffer fileNameBuf = new StringBuffer();
				fileNameBuf.append(tempDir);

				// 如果文件夹不存在
				File dir = new File(fileNameBuf.toString());
				if (!dir.exists()) {
					dir.mkdirs();
				}

				// 新建文件
				// 缓存本地文件名
				String localFileName = Joiner.on("_").join(fileUpload.getFilename(), UUID.randomUUID());
				// 文件网路路径
				taskParams.put(BussinessConstant.FILE_URL, LOCALURI + localFileName);
				fileNameBuf.append(localFileName);

				fileUpload.renameTo(new File(fileNameBuf.toString()));
				// 文件本地路径
				taskParams.put(FILE_PATH, fileNameBuf.toString());

				// 文件持久化时间
				long endTime = System.currentTimeMillis();
				LOGGER.info("文件 ：[{}] 持久化到本地总共花费时间：{}ms", fileUpload.getFilename(), (endTime - beginTime));
			}
		} else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
			Attribute attribute = (Attribute) data;
			// 取得任务参数
			taskParams.put(attribute.getName(), attribute.getValue());
		}
	}

	/**
	 * 文件完整性校验
	 *
	 * @param ctx
	 *            通道容器
	 * @param taskParams
	 *            参数信息
	 */
	private String fileIntegrityCheck(ChannelHandlerContext ctx, Map<String, Object> taskParams) throws Exception {

		// 返回结果
		String result = null;
		
		// 要求不严格的情况下，可以不用比较md5值
		if(!taskParams.isEmpty()) {
			return result;
		}

		// 取得源文件MD5
		String md5Source = taskParams.get(BussinessConstant.FILE_MD5).toString();
		// 如果MD5不存在
		if (Strings.isNullOrEmpty(md5Source)) {
			result = "FILE'S MD5 IS NOT EMPTY!";
			return result;
		}
		// 取得文件的MD5
		String filePath = taskParams.get(FILE_PATH).toString();
		File file = new File(filePath);
		String md5needCheck = Hashing.md5().hashBytes(Files.toByteArray(file)).toString();
		// 如果文件MD5不一致
		if (!Objects.equal(md5Source, md5needCheck)) {
			result = "File integrity check failed, twice MD5 inconsistent! md5ForSourceFile : " + md5Source
					+ " md5ForReceiveFile : " + md5needCheck;
			file.delete();
			return result;
		}

		return result;
	}

}

package io.hc.service.nettyTask.thirdparty.baiduyun;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidubce.BceClientException;
import com.baidubce.BceServiceException;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.BosObject;
import com.baidubce.services.bos.model.CompleteMultipartUploadRequest;
import com.baidubce.services.bos.model.CompleteMultipartUploadResponse;
import com.baidubce.services.bos.model.CopyObjectResponse;
import com.baidubce.services.bos.model.GetObjectRequest;
import com.baidubce.services.bos.model.InitiateMultipartUploadRequest;
import com.baidubce.services.bos.model.InitiateMultipartUploadResponse;
import com.baidubce.services.bos.model.ObjectMetadata;
import com.baidubce.services.bos.model.PartETag;
import com.baidubce.services.bos.model.PutObjectResponse;
import com.baidubce.services.bos.model.UploadPartRequest;
import com.baidubce.services.bos.model.UploadPartResponse;

import io.hc.service.nettyTask.thirdparty.common.Response;

/**
 * 百度云BOS客户端
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-2.0.0
 */
public class BaiduyunBOS {

	// 日志
	private Logger log = LoggerFactory.getLogger(BaiduyunBOS.class);

	// 设置每块为 20MB
	private long partSize = 1024 * 1024 * 20L;
	
	// schemal
	private final String SCHEMAL = "http://";
	
	// 上传实例
	private BosClient bos;
	
	/**
	 * 构造一个BOS客户端对象。
	 * 
	 * @param accessid    访问OSS授权id
	 * @param accesskey   访问OSS授权key
	 */
	public BaiduyunBOS(String accessid, String accesskey){
		this(null, accessid, accesskey);
	}
	
	/**
	 * 构造一个BOS客户端对象。
	 * 
	 * @param endPoint    BOS URL(可以指向内网)
	 * @param accessid    访问BOS授权id
	 * @param accesskey   访问BOS授权key
	 */
	public BaiduyunBOS(String endPoint, String accessid, String accesskey){

		// 初始化一个BosClient
		BosClientConfiguration config = new BosClientConfiguration();
		config.setCredentials(new DefaultBceCredentials(accessid, accesskey));

		// 如果endPoint不为空
		if (endPoint != null && endPoint.length() > 0) {
			// url是否“HTTP://”开头判断
			if(!endPoint.toUpperCase().startsWith("HTTP://")){
				endPoint = SCHEMAL + endPoint;
			}
			config.setEndpoint(endPoint);
		}

		bos = new BosClient(config);
	}

	/**
	 * 上传二进制串
	 *
	 * @param bucket   空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key      对象名
	 * @param content  上传字符串
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, String content) {

		return this.putObject(bucket, key, content, null);
	}

	/**
	 * 上传二进制串(包括原始信息)。
	 *
	 * @param bucket   空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key      对象名
	 * @param content  上传字符串
	 * @param metadataMap 原始数据信息(不能有中文)
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, String content, Map<String, String> metadataMap) {

		// 创建上传Object的Metadata
		ObjectMetadata meta = new ObjectMetadata();
		if(metadataMap != null){
			meta.setUserMetadata(metadataMap);
		}

		// 必须设置ContentLength
//		meta.setContentLength(len);

		// 上传Object.
		Response ret = new Response();
		PutObjectResponse result = this.bos.putObject(bucket, key, content, meta);
		log.debug(MessageFormat.format("文件上传成功:bucket:{0},key:{1}",bucket, key));
		ret.setEtag(result.getETag());
		return ret;
	}

	/**
	 * 上传二进制串
	 *
	 * @param bucket   空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key      对象名
	 * @param bytes	   上传字节数组
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, byte[] bytes) {

		return this.putObject(bucket, key, bytes, null);
	}

	/**
	 * 上传二进制串(包括原始信息)。
	 *
	 * @param bucket   空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key      对象名
	 * @param bytes	   上传字节数组
	 * @param metadataMap 原始数据信息(不能有中文)
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, byte[] bytes, Map<String, String> metadataMap) {

		// 创建上传Object的Metadata
		ObjectMetadata meta = new ObjectMetadata();
		if(metadataMap != null){
			meta.setUserMetadata(metadataMap);
		}

		// 必须设置ContentLength
//		meta.setContentLength(len);

		// 上传Object.
		Response ret = new Response();
		PutObjectResponse result = this.bos.putObject(bucket, key, bytes, meta);
		log.debug(MessageFormat.format("文件上传成功:bucket:{0},key:{1}",bucket, key));
		ret.setEtag(result.getETag());
		return ret;
	}

	/**
	 * 文件上传
	 *
	 * @param bucket   空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key      对象名
	 * @param file	   上传文件
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, File file) throws Exception {
		return this.putObject(bucket, key, file, null);
	}

	/**
	 * 上传文件(包括原始信息)。
	 *
	 * @param bucket   空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key      对象名
	 * @param file	   上传文件
	 * @param metadataMap 原始数据信息(不能有中文)
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, File file, Map<String, String> metadataMap) throws Exception {

		// 取得文件长度
		long len = file.length();

		// 超过20M，则分块上传
		if(len > this.getPartSize()){
//			ObjectMetadataInfo matadata = new ObjectMetadataInfo();
//			matadata.setUserMetadata(metadataMap);
			// 获取文件流
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				return this.putObjectByPart(bucket, key, fis, len);
			} catch (FileNotFoundException e) {
				log.error("上传百度云异常:" + e);
				throw new RuntimeException(e);
			} finally{
				try {
					if(fis != null){
						fis.close();
					}
                } catch (IOException e) {
	                e.printStackTrace();
                }
			}
		}

		// 创建上传Object的Metadata
		ObjectMetadata meta = new ObjectMetadata();
		if(metadataMap != null){
			meta.setUserMetadata(metadataMap);
		}

		// 必须设置ContentLength
		meta.setContentLength(len);

		// 上传Object.
		Response ret = new Response();
		PutObjectResponse result = this.bos.putObject(bucket, key, file, meta);
		log.debug(MessageFormat.format("文件上传成功:bucket:{0},key:{1}",bucket, key));
		ret.setEtag(result.getETag());
		return ret;
	}

	/**
	 * 上传输入流
	 *
	 * @param bucket   空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key      对象名
	 * @param in	   上传文件输入流
	 * @param len      文件长度
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, InputStream in, long len) throws Exception {
		return this.putObject(bucket, key, in, len, null);
	}
	
	/**
	 * 上传输入流(包括原始信息)。
	 * 
	 * @param bucket   空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key      对象名
	 * @param in	   上传文件输入流
	 * @param len      文件长度
	 * @param metadataMap 原始数据信息(不能有中文)
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, InputStream in, long len, Map<String, String> metadataMap) throws Exception {
		// 超过20M，则分块上传
		if(len > this.getPartSize()){
//			ObjectMetadataInfo matadata = new ObjectMetadataInfo();
//			matadata.setUserMetadata(metadataMap);
			return this.putObjectByPart(bucket, key, in, len);
		}
		
	    // 创建上传Object的Metadata
	    ObjectMetadata meta = new ObjectMetadata();
	    if(metadataMap != null){
	    	meta.setUserMetadata(metadataMap);
	    }
	    
	    // 必须设置ContentLength
//	    meta.setContentLength(len);

	    // 上传Object.
		Response ret = new Response();
		PutObjectResponse result = this.bos.putObject(bucket, key, in, meta);
	    log.debug(MessageFormat.format("文件上传成功:bucket:{0},key:{1}",bucket, key));
	    ret.setEtag(result.getETag());
		return ret;
	}
	
	/**
	 * 根据空间名和对象名取得object对象信息。
	 *
	 * @param bucket 空间名
	 * @param key 对象名
	 * @return 对象信息
	 */
	public BosObject getObject(String bucket, String key) throws Exception {
		try{
			// 获取Object，返回结果为OSSObject对象
			return this.bos.getObject(bucket, key);
		} catch (BceServiceException bce) {
			log.error("百度云服务端异常：" + bce);

			// 当object不存在的时候抛出该异常
			if(bce.getErrorCode().equals("NoSuchKey")){
				log.info(MessageFormat.format("文件不存在:bucket:{0},key:{1}",bucket, key));
				return null;
			}

			throw bce;
		} catch (BceClientException bce) {
			log.error("百度云客户端异常：" + bce);
			throw bce;
		}
	}


	/**
	 * 根据空间名和对象名取得object对象信息，将它直接存储为以file为参数的本地文件。
	 *
	 * @param bucket 空间名
	 * @param key 对象名
	 * @param filePath 输出文件路径
	 * @return 对象信息
	 */
	public ObjectMetadata getObjectToFile(String bucket, String key, String filePath) throws Exception {

		File file = null;
		ObjectMetadata om = null;
		try {
			file = new File(filePath);
			om = bos.getObject(bucket, key, file);
		} catch (BceServiceException bce) {
			log.error("百度云服务端异常：" + bce);
			if(null != file && file.exists()){
				file.delete();
			}
			throw bce;
		} catch (BceClientException bce) {
			log.error("百度云客户端异常：" + bce);
			if(null != file && file.exists()){
				file.delete();
			}
			throw bce;
		} catch (Exception bce) {
			log.error("百度云客户端异常：" + bce);
			if(null != file && file.exists()){
				file.delete();
			}
			throw bce;
		}
		return om;
	}


	/**
	 * 根据空间名和对象名取得object元数据信息。
	 *
	 * @param bucket 空间名
	 * @param key 对象名
	 * @return 元数据信息
	 */
	public ObjectMetadata getObjectMetadata(String bucket, String key){
		return bos.getObjectMetadata(bucket, key);
	}


	/**
	 * 根据空间名和对象名取得object对象范围信息。
	 *
	 * @param bucket 空间名
	 * @param key    对象名
	 * @param from   开始开始
	 * @param to     结束字节
	 * @return 对象信息
	 */
	public BosObject  getObjectByRange(String bucket, String key, long from, long to){
		// 新建GetObjectRequest
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);

		// 获取0~100字节范围内的数据
		getObjectRequest.setRange(from, to);

		// 获取Object，返回结果为BosObject对象
		return bos.getObject(getObjectRequest);
	}
	

	/**
	 * 在OSS存储上复制一份数据。
	 *
	 * @param srcBucketName  源空间名
	 * @param srcKey         源对象名
	 * @param destBucketName 目的空间名
	 * @param destKey        目的对象名
	 */
	public Response copyObject(String srcBucketName, String srcKey, String destBucketName, String destKey) {
		CopyObjectResponse result = this.bos.copyObject(srcBucketName, srcKey, destBucketName, destKey);
		Response ret = new Response();
		ret.setEtag(result.getETag());
		ret.setLastModified(result.getLastModified());
		return ret;
	}
	
	
	/**
	 * 删除对象。
	 *
	 * @param bucket 空间名
	 * @param key    对象名
	 */
	public void deleteObject(String bucket, String key) {
		this.bos.deleteObject(bucket, key);
	}


	/**
	 * 除了通过putObject接口上传文件到BOS以外，BOS还提供了另外一种上传模式 —— Multipart Upload。
	 * 用户可以在如下的应用场景内（但不仅限于此），使用Multipart Upload上传模式，如：
	 * 需要支持断点上传。
	 * 上传超过5GB大小的文件。
	 * 网络条件较差，和BOS的服务器之间的连接经常断开。
	 * 需要流式地上传文件。
	 * 上传文件之前，无法确定上传文件的大小。
	 *
	 * @param bucket   空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key      对象名
	 * @param in	   上传输入流
	 * @param len      文件字节长度
	 * @return 分段上传结果
	 */
	public Response putObjectByPart(String bucket, String key, InputStream in, long len) throws Exception {

		// 开始Multipart Upload
		InitiateMultipartUploadRequest initiateMultipartUploadRequest =
				new InitiateMultipartUploadRequest(bucket, key);
		InitiateMultipartUploadResponse initiateMultipartUploadResponse =
				bos.initiateMultipartUpload(initiateMultipartUploadRequest);

		// 打印UploadId
		log.debug("UploadId: " + initiateMultipartUploadResponse.getUploadId());

		// 获取文件流，先存入buf中，因为要2次打开（不能打开已经关闭的文件流2次,故要先写入内存中）
		byte[] buf = IOUtils.toByteArray(in);

		// 计算分块数目
		int partCount = (int) (len / partSize);
		if (len % partSize != 0){
			partCount++;
		}

		// 新建一个List保存每个分块上传后的ETag和PartNumber
		List<PartETag> partETags = new ArrayList<PartETag>();

		for(int i = 0; i < partCount; i++){
			InputStream fis = null;
			try{

				fis = new ByteArrayInputStream(buf);

				// 跳到每个分块的开头
				long skipBytes = partSize * i;
				fis.skip(skipBytes);

				// 计算每个分块的大小
				long size = partSize < len - skipBytes ?
						partSize : len - skipBytes;

				// 创建UploadPartRequest，上传分块
				UploadPartRequest uploadPartRequest = new UploadPartRequest();
				uploadPartRequest.setBucketName(bucket);
				uploadPartRequest.setKey(key);
				uploadPartRequest.setUploadId(initiateMultipartUploadResponse.getUploadId());
				uploadPartRequest.setInputStream(fis);
				uploadPartRequest.setPartSize(size);
				uploadPartRequest.setPartNumber(i + 1);
				UploadPartResponse uploadPartResponse = bos.uploadPart(uploadPartRequest);

				// 将返回的PartETag保存到List中。
				partETags.add(uploadPartResponse.getPartETag());
			}catch(FileNotFoundException e){
				log.error(MessageFormat.format("文件上传百度云失败:bucket:{0},uploadId:{1},key:{2},partNum{3}",
						bucket, initiateMultipartUploadResponse.getUploadId(), key, i + 1), e);
				throw new RuntimeException(e);
			}catch(IOException e){
				log.error(MessageFormat.format("文件上传百度云失败:bucket:{0},uploadId:{1},key:{2},partNum{3}",
						bucket, initiateMultipartUploadResponse.getUploadId(), key, i + 1), e);
				throw new RuntimeException(e);
			}finally{
				// 关闭文件
				try {
					fis.close();
				} catch (IOException e) {
					log.error(MessageFormat.format("文件上传百度云失败:bucket:{0},uploadId:{1},key:{2},partNum{3}",
							bucket, initiateMultipartUploadResponse.getUploadId(), key, i + 1), e);
					throw new RuntimeException(e);
				}
			}
		}

		CompleteMultipartUploadRequest completeMultipartUploadRequest =
				new CompleteMultipartUploadRequest(bucket, key, initiateMultipartUploadResponse.getUploadId(), partETags);

		// 完成分块上传
		CompleteMultipartUploadResponse completeMultipartUploadResponse =
				bos.completeMultipartUpload(completeMultipartUploadRequest);

		// 打印Object的ETag
		log.debug(completeMultipartUploadResponse.getETag());
		Response ret = new Response();
		ret.setBucketName(completeMultipartUploadResponse.getBucketName());
		ret.setKey(completeMultipartUploadResponse.getKey());
		ret.setEtag(completeMultipartUploadResponse.getETag());
		ret.setLocation(completeMultipartUploadResponse.getLocation());
		return ret;
	}


	public long getPartSize() {
		return partSize;
	}


	public void setPartSize(long partSize) {
		this.partSize = partSize;
	}
}

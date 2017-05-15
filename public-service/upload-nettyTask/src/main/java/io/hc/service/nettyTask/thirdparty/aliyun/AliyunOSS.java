package io.hc.service.nettyTask.thirdparty.aliyun;

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

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadResult;
import com.aliyun.oss.model.CopyObjectResult;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;

import io.hc.service.nettyTask.thirdparty.common.Response;

/**
 * 阿里云OSS客户端。
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-2.0.0
 */
public class AliyunOSS {

	// 日志
	private Logger log = LoggerFactory.getLogger(AliyunOSS.class);

	// 分块上传默认设置每块为 20M
	private int partSize = 1024 * 1024 * 20;

	// schemal
	private final String SCHEMAL = "http://";

	// 上传实例
	private OSSClient oss;

	/**
	 * 构造一个OSS客户端对象。
	 * 
	 * @param accessid
	 *            访问OSS授权id
	 * @param accesskey
	 *            访问OSS授权key
	 */
	public AliyunOSS(String accessid, String accesskey) {
		oss = new OSSClient(accessid, accesskey);
	}

	/**
	 * 构造一个OSS客户端对象。 访问object的url为[bucket + "." +
	 * "oss-cn-hangzhou.aliyuncs.com"]。
	 * 
	 * @param endPoint
	 *            OSS URL(可以指向内网)
	 * @param accessid
	 *            访问OSS授权id
	 * @param accesskey
	 *            访问OSS授权key
	 */
	public AliyunOSS(String endPoint, String accessid, String accesskey) {
		if (!endPoint.toUpperCase().startsWith("HTTP://")) {
			endPoint = SCHEMAL + endPoint;
		}

		oss = new OSSClient(endPoint, accessid, accesskey);
	}

	/**
	 * 文件上传。
	 * 
	 * @param bucket
	 *            空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key
	 *            对象名对象
	 * @param file
	 *            输入文件
	 * @param len
	 *            文件路径
	 * @param metadataMap
	 *            原始数据信息(不能有中文)
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, File file, long len, Map<String, String> metadataMap) {
		InputStream content = null;
		try {
			content = new FileInputStream(file);
			return this.putObject(bucket, key, content, file.length(), metadataMap);
		} catch (FileNotFoundException e) {
			log.error(MessageFormat.format("文件不存在:{0}", file.getAbsoluteFile()), e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (content != null) {
					content.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 文件上传。
	 * 
	 * @param bucket
	 *            空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key
	 *            对象名
	 * @param filePath
	 *            上传文件路径
	 * @param metadataMap
	 *            原始数据信息(不能有中文)
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, String filePath, Map<String, String> metadataMap) {
		File file = new File(filePath);
		return this.putObject(bucket, key, file, file.length(), metadataMap);
	}

	/**
	 * 文件上传(包括原始信息)。
	 * 
	 * @param bucket
	 *            空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param key
	 *            对象名
	 * @param filePath
	 *            上传文件输入流
	 * @param len
	 *            文件长度
	 * @param metadataMap
	 *            原始数据信息(不能有中文)
	 * @return 文件的md5值
	 */
	public Response putObject(String bucket, String key, InputStream in, long len, Map<String, String> metadataMap) {
		// 超过20M，则分块上传
		if (len > this.getPartSize()) {
			ObjectMetadataInfo matadata = new ObjectMetadataInfo();
			matadata.setUserMetadata(metadataMap);
			return this.putObjectByPart(bucket, key, in, len, matadata);
		}

		// 创建上传Object的Metadata
		ObjectMetadata meta = new ObjectMetadata();
		if (metadataMap != null) {
			meta.setUserMetadata(metadataMap);
		}

		// 必须设置ContentLength
		meta.setContentLength(len);

		// 上传Object.
		Response ret = new Response();
		PutObjectResult result = this.oss.putObject(bucket, key, in, meta);
		log.debug(MessageFormat.format("文件上传成功:bucket:{0},key:{1}", bucket, key));
		ret.setEtag(result.getETag());
		return ret;
	}

	/**
	 * 根据空间名和对象名取得object对象信息。
	 * 
	 * @param bucket
	 *            空间名
	 * @param key
	 *            对象名
	 * @return 对象信息
	 */
	public OSSObjectInfo getObject(String bucket, String key) {
		try {
			// 获取Object，返回结果为OSSObject对象
			OSSObject object = this.oss.getObject(bucket, key);
			OSSObjectInfo ret = new OSSObjectInfo();
			ret.setObjectContent(object.getObjectContent());
			ret.setObjectMetadata(object.getObjectMetadata());
			return ret;
		} catch (OSSException e) {
			// 当object不存在的时候抛出该异常
			if (e.getErrorCode().equals("NoSuchKey")) {
				log.info(MessageFormat.format("文件不存在:bucket:{0},key:{1}", bucket, key));
				return null;
			}

			throw e;
		}
	}

	/**
	 * 根据空间名和对象名取得object对象信息，将它直接存储为以file为参数的本地文件。
	 * 
	 * @param bucket
	 *            空间名
	 * @param key
	 *            对象名
	 * @param file
	 *            输出文件路径
	 * @return 对象信息
	 */
	public ObjectMetadataInfo getObjectToFile(String bucket, String key, String file) {
		try {
			// 新建GetObjectRequest
			GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);

			// 下载Object到文件
			ObjectMetadata objectMetadata = this.oss.getObject(getObjectRequest, new File(file));

			ObjectMetadataInfo ret = new ObjectMetadataInfo();
			ret.setUserMetadata(objectMetadata.getUserMetadata());
			ret.setRawMatadata(objectMetadata.getRawMetadata());
			return ret;
		} catch (OSSException e) {
			// 当object不存在的时候抛出该异常
			if (e.getErrorCode().equals("NoSuchKey")) {
				log.info(MessageFormat.format("文件不存在:bucket:{0},key:{1}", bucket, key));
				return null;
			}

			throw e;
		}
	}

	/**
	 * 根据空间名和对象名取得object元数据信息。
	 * 
	 * @param bucket
	 *            空间名
	 * @param key
	 *            对象名
	 * @return 元数据信息
	 */
	public ObjectMetadataInfo getObjectMetadata(String bucket, String key) {
		// 获取Object，返回结果为OSSObject对象
		ObjectMetadata matadata = this.oss.getObjectMetadata(bucket, key);
		ObjectMetadataInfo ret = new ObjectMetadataInfo();
		ret.setUserMetadata(matadata.getUserMetadata());
		ret.setRawMatadata(matadata.getRawMetadata());
		return ret;
	}

	/**
	 * 根据空间名和对象名取得object对象范围信息。
	 * 
	 * @param bucket
	 *            空间名
	 * @param key
	 *            对象名
	 * @param from
	 *            开始开始
	 * @param to
	 *            结束字节
	 * @return 对象信息
	 */
	public OSSObjectInfo getObjectByRange(String bucket, String key, long from, long to) {
		// 获取Object，返回结果为OSSObject对象
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);
		getObjectRequest.setRange(from, to);
		OSSObject object = this.oss.getObject(getObjectRequest);
		OSSObjectInfo ret = new OSSObjectInfo();
		ret.setObjectContent(object.getObjectContent());
		ret.setObjectMetadata(object.getObjectMetadata());
		return ret;
	}

	/**
	 * 在OSS存储上复制一份数据。
	 * 
	 * @param srcBucketName
	 *            源空间名
	 * @param srcKey
	 *            源对象名
	 * @param destBucketName
	 *            目的空间名
	 * @param destKey
	 *            目的对象名
	 */
	public Response copyObject(String srcBucketName, String srcKey, String destBucketName, String destKey) {
		CopyObjectResult result = this.oss.copyObject(srcBucketName, srcKey, destBucketName, destKey);
		Response ret = new Response();
		ret.setEtag(result.getETag());
		ret.setLastModified(result.getLastModified());
		return ret;
	}

	/**
	 * 删除对象。
	 * 
	 * @param srcBucketName
	 *            源空间名
	 * @param srcKey
	 *            源对象名
	 * @param destBucketName
	 *            目的空间名
	 * @param destKey
	 *            目的对象名
	 */
	public void deleteObject(String bucket, String key) {
		this.oss.deleteObject(bucket, key);
	}

	/**
	 * 除了通过putObject接口上传文件到OSS以外，OSS还提供了另外一种上传模式 —— Multipart
	 * Upload。用户可以在如下的应用场景内（但不仅限于此），使用Multipart Upload上传模式，如： 1.需要支持断点上传。
	 * 2.上传超过100MB大小的文件。 3.网络条件较差，和OSS的服务器之间的链接经常断开。 4.需要流式地上传文件。
	 * 5.上传文件之前，无法确定上传文件的大小。
	 *
	 * @param bucket
	 *            空间名（bucket名字不能有下划线否则会抛出异常）
	 * @param object
	 *            对象名
	 * @param filePath
	 *            上传文件路径
	 * @param len
	 *            文件字节长度
	 * @param matadata
	 *            元数据(可以为空,不能有中文)
	 * @return 分段上传结果
	 */
	public Response putObjectByPart(String bucket, String key, InputStream in, long len, ObjectMetadataInfo matadata) {
		// 开始Multipart Upload
		InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(bucket, key);
		if (matadata != null) {
			initiateMultipartUploadRequest.setObjectMetadata(matadata);
		}

		InitiateMultipartUploadResult initiateMultipartUploadResult = this.oss
				.initiateMultipartUpload(initiateMultipartUploadRequest);

		// 设置每块为 默认20M
		final int partSize = this.partSize;

		// 获取文件流，先存入buf中，因为要2次打开（不能打开已经关闭的文件流2次,故要先写入内存中）
		byte[] buf;
		try {
			buf = IOUtils.toByteArray(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// 计算分块数目
		int partCount = (int) (len / partSize);
		if (len % partSize != 0) {
			partCount++;
		}

		// 新建一个List保存每个分块上传后的ETag和PartNumber
		List<PartETag> partETags = new ArrayList<PartETag>();

		// 获取文件流
		for (int i = 0; i < partCount; i++) {
			InputStream fis = null;
			try {
				// 跳到每个分块的开头
				fis = new ByteArrayInputStream(buf);
				long skipBytes = partSize * i;
				fis.skip(skipBytes);
				// 计算每个分块的大小
				long size = partSize < len - skipBytes ? partSize : len - skipBytes;
				// 创建UploadPartRequest，上传分块
				UploadPartRequest uploadPartRequest = new UploadPartRequest();
				uploadPartRequest.setBucketName(bucket);
				uploadPartRequest.setKey(key);
				uploadPartRequest.setUploadId(initiateMultipartUploadResult.getUploadId());
				uploadPartRequest.setInputStream(fis);
				uploadPartRequest.setPartSize(size);
				uploadPartRequest.setPartNumber(i + 1);
				UploadPartResult uploadPartResult = this.oss.uploadPart(uploadPartRequest);
				partETags.add(uploadPartResult.getPartETag());
			} catch (FileNotFoundException e) {
				log.error(MessageFormat.format("文件上传失败:bucket:{0},uploadId:{1},key:{2},partNum{3}", bucket,
						initiateMultipartUploadResult.getUploadId(), key, i + 1), e);
				throw new RuntimeException(e);
			} catch (IOException e) {
				log.error(MessageFormat.format("文件上传失败:bucket:{0},uploadId:{1},key:{2},partNum{3}", bucket,
						initiateMultipartUploadResult.getUploadId(), key, i + 1), e);
				throw new RuntimeException(e);
			} finally {
				// 关闭文件
				try {
					fis.close();
				} catch (IOException e) {
					log.error(MessageFormat.format("文件上传失败:bucket:{0},uploadId:{1},key:{2},partNum{3}", bucket,
							initiateMultipartUploadResult.getUploadId(), key, i + 1), e);
					throw new RuntimeException(e);
				}
			}
		}

		CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(bucket, key,
				initiateMultipartUploadResult.getUploadId(), partETags);
		// 完成分块上传
		CompleteMultipartUploadResult completeMultipartUploadResult = this.oss
				.completeMultipartUpload(completeMultipartUploadRequest);
		Response ret = new Response();
		ret.setBucketName(completeMultipartUploadResult.getBucketName());
		ret.setKey(completeMultipartUploadResult.getKey());
		ret.setEtag(completeMultipartUploadResult.getETag());
		ret.setLocation(completeMultipartUploadResult.getLocation());
		return ret;
	}

	public int getPartSize() {
		return partSize;
	}

	public void setPartSize(int partSize) {
		this.partSize = partSize;
	}
}

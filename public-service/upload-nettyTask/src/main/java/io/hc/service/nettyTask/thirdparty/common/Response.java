package io.hc.service.nettyTask.thirdparty.common;

import java.util.Date;

/**
 * 访问yun后相应结果
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-2.0.0
 */
public class Response {

	/**
	 * The name of the bucket containing the completed multipart upload.
	 */
	private String bucketName;

	/**
	 * The key by which the object is stored.
	 */
	private String key;

	/**
	 * The entity tag identifying the new object. An entity tag is an opaque
	 * string that changes if and only if an object's data changes.
	 */
	private String etag;

	/**
	 * The URL identifying the new multipart object.
	 */
	private String location;

	/**
	 * 新Object的最后修改时间。
	 */
	private Date lastModified;

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
}

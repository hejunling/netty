package io.hc.service.nettyTask.thirdparty.aliyun;

import com.aliyun.oss.model.CopyObjectResult;

/**
 * OSS API 响应结果。
 * 
 * 阿里云OSS客户端响应bean。
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-2.0.0
 */
public class ResponseResult extends CopyObjectResult{
    /** 
     * The URL identifying the new multipart object. 
     * （分块上传用到）
     */
    private String location;

    // 分块上传用
    private int partNumber;
    
	public int getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
    
}

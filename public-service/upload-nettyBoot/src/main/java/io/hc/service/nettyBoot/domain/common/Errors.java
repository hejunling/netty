package io.hc.service.nettyBoot.domain.common;

/**
 * 返回错误信息
 *
 * @author 摇光
 * @version 1.0
 * @Created on 2016/8/26
 * @Copyright 杭州安存网络科技有限公司 Copyright (c) 2016
 */
public class Errors {

    /** 错误信息 */
    private String error;

    /** 具体信息 */
    private String message;

    /** 状态码 */
    private int status;

    /** 当前时间戳 */
    private long timestamp = System.currentTimeMillis();

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
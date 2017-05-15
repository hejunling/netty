package io.hc.service.nettyBoot.cfg;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 通知配置
 *
 * @author 摇光
 * @version 1.0
 * @Created on 2016/8/18
 * @Copyright 杭州安存网络科技有限公司 Copyright (c) 2016
 */
@ConfigurationProperties(prefix = "notice")
public class NoticeProperties {

    /** 短信邮件服务转发组件地址 */
    private String url;

    /** 邮件接收人列表 */
    private String emails;

    /** 短信接收人列表 */
    private String phones;

    /** 一天允许短信通知最大次数 */
    private int maxSendTimes = 3;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public String getPhones() {
        return phones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }

    public int getMaxSendTimes() {
        return maxSendTimes;
    }

    public void setMaxSendTimes(int maxSendTimes) {
        this.maxSendTimes = maxSendTimes;
    }
}

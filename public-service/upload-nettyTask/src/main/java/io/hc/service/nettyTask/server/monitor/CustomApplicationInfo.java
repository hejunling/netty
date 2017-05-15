package io.hc.service.nettyTask.server.monitor;

import org.springframework.stereotype.Component;

/**
 * 自定义应用信息
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
@Component
public class CustomApplicationInfo {

    /**
     * 默认不提供自定义应用信息
     *
     * @return
     */
    public String supplyCustomApplicationInfo() {
        return null;
    }

}

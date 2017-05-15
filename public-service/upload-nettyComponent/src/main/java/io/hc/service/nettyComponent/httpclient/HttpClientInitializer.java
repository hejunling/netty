package io.hc.service.nettyComponent.httpclient;

import java.util.concurrent.TimeUnit;

import io.hc.service.nettyComponent.common.PipelineInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * netty http client进出管道初始化
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-3.0.0
 */
public class HttpClientInitializer extends PipelineInitializer {

    // 读取超时设置
    private int readTimeout = -1;

    public HttpClientInitializer() {
        super();
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public HttpClientInitializer setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    protected void addOwnHandlers(ChannelPipeline pipeline) {

        pipeline.addLast(new HttpClientCodec());

        if (readTimeout > 0) {
            pipeline.addLast(new ReadTimeoutHandler(readTimeout,
                    TimeUnit.MILLISECONDS));
        }

    }
}

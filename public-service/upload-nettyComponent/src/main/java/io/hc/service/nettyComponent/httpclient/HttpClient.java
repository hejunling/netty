package io.hc.service.nettyComponent.httpclient;

import com.google.common.base.Objects;
import com.google.common.io.ByteStreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 使用netty封装http客户端
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-3.0.0
 */
public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    /**
     * get方式发送请求
     *
     * @param uri               请求地址
     * @param charset           编码
     * @return                  结果
     */
    public static String get(String uri, Charset charset) {

        try {
            return excute(HttpMethod.GET, new URI(uri), null, charset);
        } catch (URISyntaxException e) {
            logger.error("发送GET请求时发生异常：", e);
            return null;
        }

    }

    /**
     * post方式发送请求
     *
     * @param uri               请求地址
     * @param requestContent    请求体内容
     * @param charset           编码
     * @return                  结果
     */
    public static String post(String uri, String requestContent, Charset charset) {

        byte[] content = requestContent.getBytes(charset);

        return post(uri, content, charset);
    }

    /**
     * post方式发送请求
     *
     * @param uri               请求地址
     * @param requestContent    请求体内容
     * @param charset           编码
     * @return                  结果
     */
    public static String post(String uri, byte[] requestContent, Charset charset) {

        try {
            return excute(HttpMethod.POST, new URI(uri), requestContent, charset);
        } catch (URISyntaxException e) {
            logger.error("发送POST请求时发生异常：", e);
            return null;
        }

    }

    /**
     * 执行请求取得返回值
     *
     * @param method            请求类型
     * @param uri               请求地址
     * @param requestContent    请求体内容
     * @param charset           编码
     * @return                  结果
     */
    private static String excute(HttpMethod method, URI uri, byte[] requestContent, Charset charset) {

        ClientHttpRequst client = null;

        String result = "";

        try {
            // 执行请求
            client = ClientHttpRequst.bulid().setUri(uri)
                    .setMethod(method).setRequestContent(requestContent).execute();

            // 取得response
            ClientHttpResponse httpResponse = client.getHttpResponse();

            // 请求成功
            if (Objects.equal(httpResponse.getStatus(), HttpResponseStatus.OK)) {
                byte[] responses = ByteStreams.toByteArray(httpResponse.getBody());
                result = new String(responses, charset);
            }
            // 请求失败
            else {
                throw new RuntimeException(httpResponse.getStatusText());
            }

        } catch (InterruptedException | ExecutionException | IOException | RuntimeException e) {
            logger.error("请求发生异常：", e);
        } finally {
            try {
                client.close();
            } catch (ExecutionException | InterruptedException e) {
                logger.error("关闭连接时发生异常：", e);
            }
        }

        return result;
    }

}

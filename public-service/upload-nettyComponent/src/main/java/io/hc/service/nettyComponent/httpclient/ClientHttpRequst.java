package io.hc.service.nettyComponent.httpclient;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

import io.hc.service.nettyComponent.common.NettyBootstrapFactory;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 使用netty封装http客户端时的Requst
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-3.0.0
 */
public class ClientHttpRequst {

    public static final Logger logger = LoggerFactory.getLogger(ClientHttpRequst.class);

    /** 请求默认名 */
    public static final String DEFAULT_NAME = "ClientHttpRequst";

    /** 连接管道组 */
    private static final ChannelGroup allChannels = new DefaultChannelGroup(DEFAULT_NAME, GlobalEventExecutor.INSTANCE);

    /** netty启动器工厂 */
    private final NettyBootstrapFactory bootstrapFactory = new NettyBootstrapFactory();

    /** netty 启动用 通道初始化类 */
    private final HttpClientInitializer initializer = new HttpClientInitializer();

    /** 请求地址 */
    private URI uri;

    /** Http请求方式 */
    private HttpMethod method;

    /** Http请求体头部 */
    private HttpHeaders httpHeaders;

    /** 请求体内容 */
    private byte[] requestContent;

    /** 请求结果 */
    private ClientHttpResponse httpResponse;

    public static ClientHttpRequst bulid(){
        return new ClientHttpRequst();
    }

    public static ClientHttpRequst bulid(URI uri, HttpMethod method, HttpHeaders httpHeaders, byte[] bytes){
        return new ClientHttpRequst(uri, method, httpHeaders, bytes);
    }

    public ClientHttpRequst() {
    }

    public ClientHttpRequst(URI uri, HttpMethod method, HttpHeaders httpHeaders, byte[] requestContent) {
        this.uri = uri;
        this.method = method;
        this.httpHeaders = httpHeaders;
        this.requestContent = requestContent;
    }

    /**
     * 执行请求
     *
     * @return  返回本身
     * @throws InterruptedException 中断异常
     * @throws ExecutionException   执行异常
     */
    public ClientHttpRequst execute() throws InterruptedException, ExecutionException {

        final SettableFuture<ClientHttpResponse> responseFuture = SettableFuture.create();

        // 连接通道
        ChannelFuture channelFuture = bootstrapFactory.newClientBootstrap()
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(initializer.addRequestHandler(new RequestExecuteHandler(responseFuture)))
                .connect(this.uri.getHost(), getPort(this.uri)).sync();

        // 保存通道
        allChannels.add(channelFuture.channel());

        // 发送数据
        FullHttpRequest nettyRequest = createFullHttpRequest(requestContent);
        channelFuture.channel().writeAndFlush(nettyRequest);

        // 取得回馈信息
        httpResponse = responseFuture.get();

        return this;
    }

    /**
     * 注销方法
     *
     * @throws ExecutionException   执行异常
     * @throws InterruptedException 中断异常
     */
    public void close() throws ExecutionException, InterruptedException {
        httpResponse.close();
        ChannelGroupFuture channelFuture = allChannels.close();
        bootstrapFactory.shutdownGracefully(false);
        channelFuture.awaitUninterruptibly();
    }

    public URI getUri() {
        return uri;
    }

    public ClientHttpRequst setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public ClientHttpRequst setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public ClientHttpRequst setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }

    public byte[] getRequestContent() {
        return requestContent;
    }

    public ClientHttpRequst setRequestContent(byte[] bytes) {
        this.requestContent = bytes;
        return this;
    }

    public ClientHttpResponse getHttpResponse() {
        return httpResponse;
    }

    /**
     * 将发送内容写进请求实例
     *
     * @param bytes 需发送内容
     * @return      请求实例
     */
    private FullHttpRequest createFullHttpRequest(byte[] bytes) {

        bytes = bytes == null ? new byte[0] : bytes;

        FullHttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                this.method, this.uri.toASCIIString(), Unpooled.wrappedBuffer(bytes));

        nettyRequest.headers().set(HttpHeaders.Names.HOST, uri.getHost());
        nettyRequest.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        nettyRequest.headers().set(HttpHeaders.Names.CONTENT_LENGTH, nettyRequest.content().readableBytes());

        if (httpHeaders != null) {
            nettyRequest.headers().add(httpHeaders);
        }

        return nettyRequest;
    }

    /**
     * 从地址中取得端口
     *
     * @param uri   请求地址
     * @return      端口
     */
    private static int getPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(uri.getScheme())) {
                port = 80;
            }
            else if ("https".equalsIgnoreCase(uri.getScheme())) {
                port = 443;
            }
        }
        return port;
    }

    /**
     * A SimpleChannelInboundHandler to update the given SettableFuture.
     */
    private static class RequestExecuteHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

        private final SettableFuture<ClientHttpResponse> responseFuture;

        public RequestExecuteHandler(SettableFuture<ClientHttpResponse> responseFuture) {
            this.responseFuture = responseFuture;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext context, FullHttpResponse response) throws Exception {
            this.responseFuture.set(new ClientHttpResponse(context, response));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
            this.responseFuture.setException(cause);
        }
    }

}

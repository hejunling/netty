package io.hc.service.nettyComponent.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

/**
 * netty启动器构造工厂
 * 
 * @author hechuan
 *
 * @created 2017年4月10日
 *
 * @since UPLOAD-3.0.0
 */
public class NettyBootstrapFactory {
	
	/** IO线程接收线程组 */
	private EventLoopGroup bossGroup;
	
	/** IO线程工作线程组 */
	private EventLoopGroup workerGroup;

	/**
	 * 构造netty http服务端启动器
	 *
	 * @param ioThreadCount
	 *            IO工作线程数
	 * @return netty http服务端启动器
	 */
	public ServerBootstrap newServerBootstrap(int ioThreadCount) {
		if (Epoll.isAvailable()) {
			return newEpollServerBootstrap(ioThreadCount);
		}

		return newNioServerBootstrap(ioThreadCount);
	}

	/**
	 * 构造netty http客户端启动器
	 *
	 * @return netty http客户端启动器
	 */
	public Bootstrap newClientBootstrap() {

		if (Epoll.isAvailable()) {
			return newEpollClientBootstrap();
		}

		return newNioClientBootstrap();
	}

	/**
	 * 优雅的关闭netty应用
	 *
	 * @param shouldWait
	 *            是否需要等待
	 */
	public void shutdownGracefully(boolean shouldWait) {

		if (workerGroup != null) {
			Future<?> workerFuture = workerGroup.shutdownGracefully();
			if (shouldWait) {
				workerFuture.awaitUninterruptibly();
			}
		}

		if (null != bossGroup) {
			Future<?> bossFuture = bossGroup.shutdownGracefully();
			if (shouldWait) {
				bossFuture.awaitUninterruptibly();
			}
		}
	}

	/**
	 * 构建NIO线程类型的netty启动器
	 *
	 * @param ioThreadCount
	 *            工作线程数
	 * @return NIO线程类型的netty启动器
	 */
	private ServerBootstrap newNioServerBootstrap(int ioThreadCount) {

		bossGroup = new NioEventLoopGroup(1);

		if (ioThreadCount > 0) {
			workerGroup = new NioEventLoopGroup(ioThreadCount);
		} else {
			workerGroup = new NioEventLoopGroup();
		}

		return new ServerBootstrap().group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
	}

	private ServerBootstrap newEpollServerBootstrap(int ioThreadCount) {
		bossGroup = new EpollEventLoopGroup(1);

		if (ioThreadCount > 0) {
			workerGroup = new EpollEventLoopGroup(ioThreadCount);
		} else {
			workerGroup = new EpollEventLoopGroup();
		}

		return new ServerBootstrap().group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class);
	}

	private Bootstrap newNioClientBootstrap() {
		workerGroup = new NioEventLoopGroup();

		return new Bootstrap().group(workerGroup).channel(NioSocketChannel.class);
	}

	private Bootstrap newEpollClientBootstrap() {
		workerGroup = new EpollEventLoopGroup();

		return new Bootstrap().group(workerGroup).channel(EpollSocketChannel.class);
	}
}

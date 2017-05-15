package io.hc.service.nettyComponent.httpserver;

/**
 * @author toddf
 * @since May 31, 2012
 */
public class SocketSettings {
	private boolean useTcpNoDelay = true;
	private int soLinger = -1; // disabled by default
	private int receiveBufferSize = 262140; // Java default
	private int connectTimeoutMillis = 10000; // netty default

	public boolean useTcpNoDelay() {
		return useTcpNoDelay;
	}

	public void setUseTcpNoDelay(boolean useTcpNoDelay) {
		this.useTcpNoDelay = useTcpNoDelay;
	}

	public int getSoLinger() {
		return soLinger;
	}

	public void setSoLinger(int soLinger) {
		this.soLinger = soLinger;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}

	public int getConnectTimeoutMillis() {
		return connectTimeoutMillis;
	}

	public void setConnectTimeoutMillis(int connectTimeoutMillis) {
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

}

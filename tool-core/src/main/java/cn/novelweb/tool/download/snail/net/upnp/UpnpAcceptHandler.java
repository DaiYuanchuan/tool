package cn.novelweb.tool.download.snail.net.upnp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import cn.novelweb.tool.download.snail.net.UdpAcceptHandler;
import cn.novelweb.tool.download.snail.net.UdpMessageHandler;

/**
 * <p>UPNP接收器</p>
 * 
 * @author acgist
 */
public final class UpnpAcceptHandler extends UdpAcceptHandler {

	private static final UpnpAcceptHandler INSTANCE = new UpnpAcceptHandler();
	
	public static UpnpAcceptHandler getInstance() {
		return INSTANCE;
	}
	
	private UpnpAcceptHandler() {
	}

	/**
	 * <p>UPNP消息代理</p>
	 */
	private final UpnpMessageHandler upnpMessageHandler = new UpnpMessageHandler();
	
	@Override
	public UdpMessageHandler messageHandler(ByteBuffer buffer, InetSocketAddress socketAddress) {
		return this.upnpMessageHandler;
	}

}

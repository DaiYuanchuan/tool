package cn.novelweb.tool.download.snail.net.upnp;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.net.UdpClient;
import cn.novelweb.tool.download.snail.pojo.wrapper.HeaderWrapper;
import cn.novelweb.tool.download.snail.utils.NetUtils;

/**
 * <p>UPNP客户端</p>
 * 
 * @author acgist
 */
public final class UpnpClient extends UdpClient<UpnpMessageHandler> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpClient.class);

	/**
	 * <p>M-SEARCH协议：{@value}</p>
	 */
	private static final String PROTOCOL = "M-SEARCH * HTTP/1.1";
	
	/**
	 * @param socketAddress 地址
	 */
	private UpnpClient(InetSocketAddress socketAddress) {
		super("UPNP Client", new UpnpMessageHandler(), socketAddress);
	}
	
	public static final UpnpClient newInstance() {
		return new UpnpClient(NetUtils.buildSocketAddress(UpnpServer.UPNP_HOST, UpnpServer.UPNP_PORT));
	}

	@Override
	public boolean open() {
		return this.open(UpnpServer.getInstance().channel());
	}
	
	/**
	 * <p>发送M-SEARCH消息</p>
	 */
	public void mSearch() {
		LOGGER.debug("发送M-SEARCH消息");
		try {
			this.send(this.buildMSearch());
		} catch (NetException e) {
			LOGGER.error("发送M-SEARCH消息异常", e);
		}
	}
	
	/**
	 * <p>创建M-SEARCH消息</p>
	 * 
	 * @return 消息
	 */
	private String buildMSearch() {
		final HeaderWrapper builder = HeaderWrapper.newBuilder(PROTOCOL);
		builder
			.header("HOST", UpnpServer.UPNP_HOST + ":" + UpnpServer.UPNP_PORT)
			.header("ST", UpnpServer.UPNP_ROOT_DEVICE)
			.header("MAN", "\"ssdp:discover\"")
			.header("MX", "3");
		return builder.build();
	}

}

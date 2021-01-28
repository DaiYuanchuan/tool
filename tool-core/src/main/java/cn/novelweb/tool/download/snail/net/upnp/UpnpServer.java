package cn.novelweb.tool.download.snail.net.upnp;

import cn.novelweb.tool.download.snail.net.UdpServer;

/**
 * <p>UPNP服务端</p>
 * 
 * @author acgist
 */
public final class UpnpServer extends UdpServer<UpnpAcceptHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpServer.class);
	
	private static final UpnpServer INSTANCE = new UpnpServer();
	
	public static UpnpServer getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>TTL</p>
	 */
	private static final int UPNP_TTL = 2;
	/**
	 * <p>UPNP端口</p>
	 */
	public static int UPNP_PORT = 1900;
	/**
	 * <p>UPNP地址</p>
	 */
	public static String UPNP_HOST = "239.255.255.250";
	/**
	 * <p>UPNP根设备</p>
	 */
	public static String UPNP_ROOT_DEVICE = "upnp:rootdevice";
	
	private UpnpServer() {
		// 不监听UPNP端口：防止收到很多其他应用消息
		super("UPNP Server", UpnpAcceptHandler.getInstance());
		this.join(UPNP_TTL, UPNP_HOST);
		this.handle();
	}
	
}

package cn.novelweb.tool.download.snail.net.torrent.tracker;

import cn.novelweb.tool.download.snail.net.UdpServer;

/**
 * <p>Tracker服务端</p>
 * 
 * @author acgist
 */
public final class TrackerServer extends UdpServer<TrackerAcceptHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerServer.class);
	
	private static final TrackerServer INSTANCE = new TrackerServer();
	
	public static final TrackerServer getInstance() {
		return INSTANCE;
	}
	
	private TrackerServer() {
		super("Tracker Server", TrackerAcceptHandler.getInstance());
		this.handle();
	}

}

package cn.novelweb.tool.download.snail.net.torrent;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.net.UdpServer;

/**
 * <p>Torrent服务端：UTP、DHT、STUN</p>
 * <p>监听端口：{@link SystemConfig#getTorrentPort()}</p>
 * 
 * @author acgist
 */
public final class TorrentServer extends UdpServer<TorrentAcceptHandler> {
	
	private static final TorrentServer INSTANCE = new TorrentServer();
	
	public static final TorrentServer getInstance() {
		return INSTANCE;
	}

	private TorrentServer() {
		super(SystemConfig.getTorrentPort(), "Torrent(UTP/DHT/STUN) Server", TorrentAcceptHandler.getInstance());
		this.handle();
	}

}

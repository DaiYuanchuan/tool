package cn.novelweb.tool.download.snail.net.torrent.utp;

import cn.novelweb.tool.download.snail.net.UdpClient;
import cn.novelweb.tool.download.snail.net.torrent.TorrentServer;
import cn.novelweb.tool.download.snail.net.torrent.peer.PeerSubMessageHandler;
import cn.novelweb.tool.download.snail.pojo.session.PeerSession;

/**
 * <p>UTP客户端</p>
 * 
 * @author acgist
 */
public final class UtpClient extends UdpClient<UtpMessageHandler> {

	/**
	 * <p>Peer信息</p>
	 */
	private final PeerSession peerSession;
	/**
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	
	/**
	 * @param peerSession Peer信息
	 * @param peerSubMessageHandler Peer消息代理
	 */
	private UtpClient(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		super("UTP Client", new UtpMessageHandler(peerSubMessageHandler, peerSession.peerSocketAddress()), peerSession.peerSocketAddress());
		this.peerSession = peerSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}
	
	/**
	 * <p>创建UTP客户端</p>
	 * 
	 * @param peerSession Peer信息
	 * @param peerSubMessageHandler Peer消息代理
	 * 
	 * @return UTP客户端
	 */
	public static final UtpClient newInstance(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new UtpClient(peerSession, peerSubMessageHandler);
	}
	
	@Override
	public boolean open() {
		return open(TorrentServer.getInstance().channel());
	}

	/**
	 * <p>连接</p>
	 * 
	 * @return true-连接成功；false-连接失败；
	 */
	public boolean connect() {
		return this.handler.connect();
	}

	/**
	 * <p>获取Peer信息</p>
	 * 
	 * @return Peer信息
	 */
	public PeerSession peerSession() {
		return this.peerSession;
	}
	
	/**
	 * <p>获取Peer消息代理</p>
	 * 
	 * @return Peer消息代理
	 */
	public PeerSubMessageHandler peerSubMessageHandler() {
		return this.peerSubMessageHandler;
	}
	
}

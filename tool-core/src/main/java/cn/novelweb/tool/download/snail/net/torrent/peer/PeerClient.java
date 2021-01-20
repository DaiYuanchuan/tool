package cn.novelweb.tool.download.snail.net.torrent.peer;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.net.TcpClient;
import cn.novelweb.tool.download.snail.pojo.session.PeerSession;

/**
 * <p>Peer客户端</p>
 * 
 * @author acgist
 */
public final class PeerClient extends TcpClient<PeerMessageHandler> {

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
	private PeerClient(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		super("Peer Client", SystemConfig.CONNECT_TIMEOUT, new PeerMessageHandler(peerSubMessageHandler));
		this.peerSession = peerSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
	}

	/**
	 * <p>创建Peer客户端</p>
	 * 
	 * @param peerSession Peer信息
	 * @param peerSubMessageHandler Peer消息代理
	 * 
	 * @return Peer客户端
	 */
	public static final PeerClient newInstance(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new PeerClient(peerSession, peerSubMessageHandler);
	}
	
	@Override
	public boolean connect() {
		return this.connect(this.peerSession.host(), this.peerSession.port());
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

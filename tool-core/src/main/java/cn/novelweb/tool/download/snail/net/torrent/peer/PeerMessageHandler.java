package cn.novelweb.tool.download.snail.net.torrent.peer;

import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.net.IMessageEncryptSender;
import cn.novelweb.tool.download.snail.net.TcpMessageHandler;

import java.nio.ByteBuffer;

/**
 * <p>Peer消息代理</p>
 * 
 * @author acgist
 */
public final class PeerMessageHandler extends TcpMessageHandler implements IMessageEncryptSender {

//	private static final Logger LOGGER = LoggerFactory.getLogger(PeerMessageHandler.class);
	
	/**
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;

	/**
	 * <p>服务端</p>
	 */
	public PeerMessageHandler() {
		this(PeerSubMessageHandler.newInstance());
	}

	/**
	 * <p>客户端</p>
	 * 
	 * @param peerSubMessageHandler Peer消息代理
	 */
	public PeerMessageHandler(PeerSubMessageHandler peerSubMessageHandler) {
		this.peerSubMessageHandler = peerSubMessageHandler;
		final PeerUnpackMessageCodec peerUnpackMessageCodec = new PeerUnpackMessageCodec(this.peerSubMessageHandler);
		final PeerCryptMessageCodec peerCryptMessageCodec = new PeerCryptMessageCodec(peerUnpackMessageCodec, this.peerSubMessageHandler);
		this.messageCodec = peerCryptMessageCodec;
		this.peerSubMessageHandler.messageEncryptSender(this);
	}
	
	@Override
	public void sendEncrypt(ByteBuffer buffer, int timeout) throws NetException {
		this.messageCodec.encode(buffer);
		this.send(buffer, timeout);
	}

}

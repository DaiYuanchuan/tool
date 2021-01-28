package cn.novelweb.tool.download.snail.net.torrent.peer.extension;

import cn.novelweb.tool.download.snail.config.PeerConfig;
import cn.novelweb.tool.download.snail.config.PeerConfig.ExtensionType;
import cn.novelweb.tool.download.snail.config.PeerConfig.HolepunchErrorCode;
import cn.novelweb.tool.download.snail.config.PeerConfig.HolepunchType;
import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.PeerContext;
import cn.novelweb.tool.download.snail.net.torrent.peer.ExtensionMessageHandler;
import cn.novelweb.tool.download.snail.net.torrent.peer.ExtensionTypeMessageHandler;
import cn.novelweb.tool.download.snail.net.torrent.peer.PeerConnect;
import cn.novelweb.tool.download.snail.net.torrent.peer.PeerSubMessageHandler;
import cn.novelweb.tool.download.snail.net.torrent.utp.UtpClient;
import cn.novelweb.tool.download.snail.pojo.session.PeerSession;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.utils.NetUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * <p>Holepunch extension</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0055.html</p>
 * <p>Pex交换的Peer如果不能直接连接，Pex源Peer作为中继通过holepunch协议实现连接。</p>
 * 
 * @author acgist
 */
public final class HolepunchMessageHnadler extends ExtensionTypeMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HolepunchMessageHnadler.class);

	/**
	 * <p>IPv4：{@value}</p>
	 */
	private static final byte IPV4 = 0x00;
	/**
	 * <p>IPv6：{@value}</p>
	 */
	private static final byte IPV6 = 0x01;
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	
	/**
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * @param extensionMessageHandler 扩展协议代理
	 */
	private HolepunchMessageHnadler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		super(ExtensionType.UT_HOLEPUNCH, peerSession, extensionMessageHandler);
		this.torrentSession = torrentSession;
	}
	
	/**
	 * <p>创建holepunch扩展协议代理</p>
	 * 
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * @param extensionMessageHandler 扩展协议代理
	 * 
	 * @return holepunch扩展协议代理
	 */
	public static HolepunchMessageHnadler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		return new HolepunchMessageHnadler(peerSession, torrentSession, extensionMessageHandler);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>发起方没有在扩展协议握手时表示支持holepunch扩展协议：中继应该忽略所有消息</p>
	 * 
	 * @see HolepunchMessageHnadler#onMessage(ByteBuffer)
	 */
	@Override
	public void doMessage(ByteBuffer buffer) {
		final byte typeId = buffer.get();
		final HolepunchType holepunchType = PeerConfig.HolepunchType.of(typeId);
		if(holepunchType == null) {
			LOGGER.warn("处理holepunch消息错误（未知类型）：{}", typeId);
			return;
		}
		int port;
		String host;
		final byte addrType = buffer.get(); // 地址类型
		if(addrType == IPV4) {
			host = NetUtils.intToIP(buffer.getInt());
			port = NetUtils.portToInt(buffer.getShort());
		} else if(addrType == IPV6) {
			// TODO：IPv6
			return;
		} else {
			LOGGER.error("处理holepunch消息错误（不支持的IP协议类型）：{}", addrType);
			return;
		}
		LOGGER.debug("处理holepunch消息：{}", holepunchType);
		switch (holepunchType) {
		case RENDEZVOUS:
			this.onRendezvous(host, port);
			break;
		case CONNECT:
			this.onConnect(host, port);
			break;
		case ERROR:
			final int errorCode = buffer.getInt();
			this.onError(host, port, errorCode);
			break;
		default:
			LOGGER.warn("处理holepunch消息错误（类型未适配）：{}", holepunchType);
			break;
		}
	}
	
	/**
	 * <p>发送消息：rendezvous</p>
	 * 
	 * @param peerSession Peer信息
	 */
	public void rendezvous(PeerSession peerSession) {
		final String host = peerSession.host();
		final int port = peerSession.port();
		LOGGER.debug("发送holepunch消息-rendezvous：{}-{}", host, port);
		this.pushMessage(this.buildMessage(HolepunchType.RENDEZVOUS, host, port));
		peerSession.lockHolepunch(); // 加锁
	}
	
	/**
	 * <p>处理消息：rendezvous</p>
	 * 
	 * @param host 目标地址
	 * @param port 目标端口
	 */
	private void onRendezvous(String host, int port) {
		LOGGER.debug("处理holepunch消息-rendezvous：{}-{}", host, port);
		final String extIp = SystemConfig.getExternalIpAddress();
		if(StringUtils.equals(host, extIp)) {
			LOGGER.debug("处理holepunch消息-rendezvous失败：目标属于中继");
			this.error(host, port, HolepunchErrorCode.CODE_04);
			return;
		}
		// 目标Peer
		final PeerSession peerSession = PeerContext.getInstance().findPeerSession(this.torrentSession.infoHashHex(), host, port);
		// 目标不存在
		if(peerSession == null) {
			LOGGER.debug("处理holepunch消息-rendezvous失败：目标不存在");
			this.error(host, port, HolepunchErrorCode.CODE_01);
			return;
		}
		// 目标未连接
		if(!peerSession.connected()) {
			LOGGER.debug("处理holepunch消息-rendezvous失败：目标未连接");
			this.error(host, port, HolepunchErrorCode.CODE_02);
			return;
		}
		// 目标不支持协议
		if(!peerSession.supportExtensionType(ExtensionType.UT_HOLEPUNCH)) {
			LOGGER.debug("处理holepunch消息-rendezvous失败：目标不支持协议");
			this.error(host, port, HolepunchErrorCode.CODE_03);
			return;
		}
		// 向发起方发送目标方连接消息
		this.connect(host, port);
		// 向目标方发送发起方连接消息
		final PeerConnect peerConnect = peerSession.peerConnect();
		if(peerConnect != null) {
			peerConnect.holepunchConnect(this.peerSession.host(), this.peerSession.port());
		} else {
			LOGGER.warn("处理holepunch消息-rendezvous失败：目标失效");
		}
	}
	
	/**
	 * <p>发送消息：connect</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 */
	public void connect(String host, int port) {
		LOGGER.debug("发送holepunch消息-connect：{}-{}", host, port);
		this.pushMessage(this.buildMessage(HolepunchType.CONNECT, host, port));
	}
	
	/**
	 * <p>处理消息：connect</p>
	 * <p>如果目标方不希望连接发起方时，直接忽略连接消息，不能响应错误给中继。</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 */
	private void onConnect(String host, int port) {
		LOGGER.debug("处理holepunch消息-connect：{}-{}", host, port);
		PeerSession peerSession = PeerContext.getInstance().findPeerSession(this.torrentSession.infoHashHex(), host, port);
		if(peerSession == null) {
			peerSession = PeerContext.getInstance().newPeerSession(
				this.torrentSession.infoHashHex(),
				this.torrentSession.statistics(),
				host,
				port,
				PeerConfig.Source.HOLEPUNCH
			);
		}
		if(peerSession.holepunchWait()) {
			// 发起方：等待响应
			LOGGER.debug("处理holepunch消息-connect：释放holepunch等待锁");
			peerSession.unlockHolepunch();
		} else {
			// 目标方：主动连接
			if(peerSession.connected()) {
				// 已经连接忽略消息：不用响应信息给中继
				LOGGER.debug("处理holepunch消息-connect：目标已连接");
				return;
			}
			// 发起连接
			final PeerSubMessageHandler peerSubMessageHandler = PeerSubMessageHandler.newInstance(peerSession, this.torrentSession);
			final UtpClient client = UtpClient.newInstance(peerSession, peerSubMessageHandler);
			if(client.connect()) {
				LOGGER.debug("处理holepunch消息-connect：连接成功");
				peerSession.flags(PeerConfig.PEX_UTP);
				client.close();
			} else {
				LOGGER.debug("处理holepunch消息-connect：连接失败");
			}
		}
	}

	/**
	 * <p>发送消息：error</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 * @param errorCode 错误编码
	 */
	private void error(String host, int port, HolepunchErrorCode errorCode) {
		LOGGER.debug("发送holepunch消息-error：{}-{}-{}", host, port, errorCode);
		this.pushMessage(this.buildMessage(HolepunchType.ERROR, host, port, errorCode));
	}
	
	/**
	 * <p>处理消息：error</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 * @param errorCode 错误编码
	 */
	private void onError(String host, int port, int errorCode) {
		LOGGER.warn("处理holepunch消息-error：{}-{}-{}", host, port, errorCode);
	}
	
	/**
	 * <p>创建消息</p>
	 * 
	 * @param type 消息类型
	 * @param host Peer地址
	 * @param port Peer端口
	 * 
	 * @return 消息
	 * 
	 * @see #buildMessage(HolepunchType, String, int, HolepunchErrorCode)
	 */
	private ByteBuffer buildMessage(HolepunchType type, String host, int port) {
		return this.buildMessage(type, host, port, null);
	}
	
	/**
	 * <p>创建消息</p>
	 * 
	 * @param type 消息类型
	 * @param host Peer地址
	 * @param port Peer端口
	 * @param errorCode 错误编码（null：没有错误消息）
	 * 
	 * @return 消息
	 * 
	 * TODO：IPv6
	 */
	private ByteBuffer buildMessage(HolepunchType type, String host, int port, HolepunchErrorCode errorCode) {
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.put(type.id()); // 消息类型
		buffer.put(IPV4); // 地址类型：0x00=IPv4；0x01=IPv6；
		buffer.putInt(NetUtils.ipToInt(host)); // IP地址
		buffer.putShort(NetUtils.portToShort(port)); // 端口号
		if(type == HolepunchType.ERROR && errorCode != null) {
			// 非错误消息不发送错误编码
			buffer.putInt(errorCode.code()); // 错误编码
		}
		return buffer;
	}
	
}

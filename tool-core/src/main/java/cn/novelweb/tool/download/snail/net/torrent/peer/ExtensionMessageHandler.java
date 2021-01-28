package cn.novelweb.tool.download.snail.net.torrent.peer;

import cn.novelweb.tool.download.snail.config.CryptConfig;
import cn.novelweb.tool.download.snail.config.PeerConfig;
import cn.novelweb.tool.download.snail.config.PeerConfig.Action;
import cn.novelweb.tool.download.snail.config.PeerConfig.ExtensionType;
import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.context.exception.PacketSizeException;
import cn.novelweb.tool.download.snail.format.BEncodeDecoder;
import cn.novelweb.tool.download.snail.format.BEncodeEncoder;
import cn.novelweb.tool.download.snail.net.torrent.peer.extension.*;
import cn.novelweb.tool.download.snail.pojo.bean.InfoHash;
import cn.novelweb.tool.download.snail.pojo.session.PeerSession;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.utils.MapUtils;
import cn.novelweb.tool.download.snail.utils.NetUtils;
import cn.novelweb.tool.download.snail.utils.NumberUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>扩展协议代理</p>
 * <p>LTEP（Libtorrent Extension Protocol）扩展协议</p>
 * <p>Extension Protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0010.html</p>
 * 
 * @author acgist
 */
public final class ExtensionMessageHandler implements IExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionMessageHandler.class);
	
	/**
	 * <p>偏爱明文：{@value}</p>
	 */
	private static final int PREFER_PLAINTEXT = 0;
	/**
	 * <p>偏爱加密：{@value}</p>
	 */
	private static final int PREFER_ENCRYPT = 1;
	/**
	 * <p>只上传不下载：{@value}</p>
	 */
	private static final int UPLOAD_ONLY = 1;
	/**
	 * <p>默认支持未完成请求数量：{@value}</p>
	 */
	private static final int DEFAULT_REQQ = 128;
	/**
	 * <p>扩展协议信息：{@value}</p>
	 */
	private static final String EX_M = "m";
	/**
	 * <p>软件信息（名称和版本）：{@value}</p>
	 */
	private static final String EX_V = "v";
	/**
	 * <p>端口：{@value}</p>
	 */
	private static final String EX_P = "p";
	/**
	 * <p>偏爱加密：{@value}</p>
	 * 
	 * @see #PREFER_PLAINTEXT
	 * @see #PREFER_ENCRYPT
	 */
	private static final String EX_E = "e";
	/**
	 * <p>支持未完成请求数量：{@value}</p>
	 * 
	 * @see #DEFAULT_REQQ
	 */
	private static final String EX_REQQ = "reqq";
//	/**
//	 * <p>IPv4地址：{@value}</p>
//	 */
//	private static final String EX_IPV4 = "ipv4";
//	/**
//	 * <p>IPv6地址：{@value}</p>
//	 */
//	private static final String EX_IPV6 = "ipv6";
	/**
	 * <p>外网IP地址：{@value}</p>
	 */
	private static final String EX_YOURIP = "yourip";
	/**
	 * <p>只上传不下载：{@value}</p>
	 * <p>任务完成只上传不下载</p>
	 * <p>协议链接：http://bittorrent.org/beps/bep_0021.html</p>
	 * 
	 * @see #UPLOAD_ONLY
	 */
	private static final String EX_UPLOAD_ONLY = "upload_only";
	/**
	 * <p>种子InfoHash数据大小：{@value}</p>
	 * <p>ut_metadata协议使用</p>
	 * 
	 * @see MetadataMessageHandler
	 */
	private static final String EX_METADATA_SIZE = "metadata_size";

	/**
	 * <p>是否已经发送握手</p>
	 */
	private volatile boolean handshakeSend = false;
	/**
	 * <p>是否已经接收握手</p>
	 */
	private volatile boolean handshakeRecv = false;
	/**
	 * <p>InfoHash</p>
	 */
	private final InfoHash infoHash;
	/**
	 * <p>Peer信息</p>
	 */
	private final PeerSession peerSession;
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	/**
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	/**
	 * @see MetadataMessageHandler
	 */
	private final MetadataMessageHandler metadataMessageHandler;
	/**
	 * @see HolepunchMessageHnadler
	 */
	private final HolepunchMessageHnadler holepunchMessageHnadler;
	/**
	 * @see PeerExchangeMessageHandler
	 */
	private final PeerExchangeMessageHandler peerExchangeMessageHandler;
	/**
	 * @see DontHaveExtensionMessageHandler
	 */
	private final DontHaveExtensionMessageHandler dontHaveExtensionMessageHandler;
	/**
	 * @see UploadOnlyExtensionMessageHandler
	 */
	private final UploadOnlyExtensionMessageHandler uploadOnlyExtensionMessageHandler;
	
	/**
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * @param peerSubMessageHandler Peer消息代理
	 */
	private ExtensionMessageHandler(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		this.infoHash = torrentSession.infoHash();
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.metadataMessageHandler = MetadataMessageHandler.newInstance(peerSession, torrentSession, this);
		this.holepunchMessageHnadler = HolepunchMessageHnadler.newInstance(peerSession, torrentSession, this);
		this.peerExchangeMessageHandler = PeerExchangeMessageHandler.newInstance(peerSession, torrentSession, this);
		this.dontHaveExtensionMessageHandler = DontHaveExtensionMessageHandler.newInstance(peerSession, this);
		this.uploadOnlyExtensionMessageHandler = UploadOnlyExtensionMessageHandler.newInstance(peerSession, this);
	}
	
	/**
	 * <p>创建扩展协议代理</p>
	 * 
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 * @param peerSubMessageHandler Peer消息代理
	 * 
	 * @return 扩展协议代理
	 */
	public static ExtensionMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, PeerSubMessageHandler peerSubMessageHandler) {
		return new ExtensionMessageHandler(peerSession, torrentSession, peerSubMessageHandler);
	}
	
	@Override
	public void onMessage(ByteBuffer buffer) throws NetException {
		final byte typeId = buffer.get();
		final ExtensionType extensionType = ExtensionType.of(typeId);
		if(extensionType == null) {
			LOGGER.warn("处理扩展消息错误（未知类型）：{}", typeId);
			return;
		}
		LOGGER.debug("处理扩展消息类型：{}", extensionType);
		switch (extensionType) {
		case HANDSHAKE:
			this.handshake(buffer);
			break;
		case UT_PEX:
			this.pex(buffer);
			break;
		case UT_METADATA:
			this.metadata(buffer);
			break;
		case UT_HOLEPUNCH:
			this.holepunch(buffer);
			break;
		case UPLOAD_ONLY:
			this.uploadOnly(buffer);
			break;
		case LT_DONTHAVE:
			this.dontHave(buffer);
			break;
		default:
			LOGGER.warn("处理扩展消息错误（类型未适配）：{}", extensionType);
			break;
		}
	}
	
	/**
	 * <p>发送握手消息</p>
	 */
	public void handshake() {
		LOGGER.debug("发送扩展消息-握手");
		this.handshakeSend = true;
		final Map<String, Object> message = new LinkedHashMap<>(); // 扩展消息
		final Map<String, Object> supportTypes = new LinkedHashMap<>(); // 支持的扩展协议
		for (ExtensionType type : PeerConfig.ExtensionType.values()) {
			if(type.support() && type.notice()) {
				supportTypes.put(type.value(), type.id());
			}
		}
		message.put(EX_M, supportTypes); // 支持的扩展协议
		// 如果已经接收握手消息：不发送TCP端口
		if(!this.handshakeRecv) {
			message.put(EX_P, SystemConfig.getTorrentPortExt()); // 外网监听TCP端口
		}
		message.put(EX_V, SystemConfig.getNameEnAndVersion()); // 客户端信息（名称、版本）
		message.put(EX_E, CryptConfig.STRATEGY.crypt() ? PREFER_ENCRYPT : PREFER_PLAINTEXT); // 偏爱加密
		// 外网IP地址：TODO：IPv6
		final String yourip = SystemConfig.getExternalIpAddress();
		if(StringUtils.isNotEmpty(yourip)) {
			message.put(EX_YOURIP, NumberUtils.intToBytes(NetUtils.ipToInt(yourip)));
		}
		message.put(EX_REQQ, DEFAULT_REQQ);
		if(PeerConfig.ExtensionType.UT_METADATA.notice()) {
			// 种子InfoHash数据长度
			final int metadataSize = this.infoHash.size();
			if(metadataSize > 0) {
				message.put(EX_METADATA_SIZE, metadataSize);
			}
		}
		// 任务已经完成：只上传不下载
		if(this.torrentSession.completed()) {
			message.put(EX_UPLOAD_ONLY, UPLOAD_ONLY);
		}
		this.pushMessage(ExtensionType.HANDSHAKE.id(), BEncodeEncoder.encodeMap(message));
	}

	/**
	 * <p>处理握手消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws PacketSizeException 网络包异常
	 */
	private void handshake(ByteBuffer buffer) throws PacketSizeException {
		LOGGER.debug("处理扩展消息-握手");
		this.handshakeRecv = true;
		final BEncodeDecoder decoder = BEncodeDecoder.newInstance(buffer);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("处理扩展消息-握手失败（格式）：{}", decoder.oddString());
			return;
		}
		// 获取端口
		final Long port = decoder.getLong(EX_P);
		if(port != null) {
			final Integer oldPort = this.peerSession.port();
			if(oldPort == null) {
				this.peerSession.port(port.intValue());
			} else if(oldPort.intValue() != port.intValue()) {
				LOGGER.debug("处理扩展消息-握手（端口不一致）：{}-{}", oldPort, port);
			}
		}
		// 偏爱加密
		final Long encrypt = decoder.getLong(EX_E);
		if(encrypt != null && encrypt.intValue() == PREFER_ENCRYPT) {
			this.peerSession.flags(PeerConfig.PEX_PREFER_ENCRYPTION);
		}
		// 只上传不下载
		final Long uploadOnly = decoder.getLong(EX_UPLOAD_ONLY);
		if(uploadOnly != null && uploadOnly.intValue() == UPLOAD_ONLY) {
			this.peerSession.flags(PeerConfig.PEX_UPLOAD_ONLY);
		}
		// 种子InfoHash数据长度
		final Long metadataSize = decoder.getLong(EX_METADATA_SIZE);
		if(metadataSize != null && this.infoHash.size() <= 0) {
			this.infoHash.size(metadataSize.intValue());
		}
		// 支持的扩展协议：扩展协议名称=扩展协议标识
		final Map<String, Object> supportTypes = decoder.getMap(EX_M);
		if(MapUtils.isNotEmpty(supportTypes)) {
			supportTypes.entrySet().forEach(entry -> {
				final Long typeId = (Long) entry.getValue();
				final String typeValue = entry.getKey();
				final PeerConfig.ExtensionType extensionType = PeerConfig.ExtensionType.of(typeValue);
				if(extensionType == PeerConfig.ExtensionType.UT_HOLEPUNCH) {
					this.peerSession.flags(PeerConfig.PEX_HOLEPUNCH);
				}
				if(extensionType != null && extensionType.support()) {
					LOGGER.debug("处理扩展协议-握手（添加）：{}-{}", extensionType, typeId);
					this.peerSession.addExtensionType(extensionType, typeId.byteValue());
				} else {
					LOGGER.debug("处理扩展协议-握手（未知协议）：{}-{}", typeValue, typeId);
				}
			});
		}
		// 发送握手
		if(!this.handshakeSend) {
			this.handshake();
		}
		// 种子文件下载
		if(this.torrentSession.action() == Action.MAGNET) {
			this.metadata();
		}
	}
	
	/**
	 * <p>发送pex消息</p>
	 * 
	 * @param bytes 消息
	 * 
	 * @see PeerExchangeMessageHandler#pex(byte[])
	 */
	public void pex(byte[] bytes) {
		if(this.peerExchangeMessageHandler.supportExtensionType()) {
			this.peerExchangeMessageHandler.pex(bytes);
		}
	}
	
	/**
	 * <p>处理pex消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see PeerExchangeMessageHandler#onMessage(ByteBuffer)
	 */
	private void pex(ByteBuffer buffer) throws NetException {
		this.peerExchangeMessageHandler.onMessage(buffer);
	}
	
	/**
	 * <p>发送metadata消息</p>
	 * 
	 * @see MetadataMessageHandler#request()
	 */
	public void metadata() {
		if(this.metadataMessageHandler.supportExtensionType()) {
			this.metadataMessageHandler.request();
		}
	}
	
	/**
	 * <p>处理metadata消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see MetadataMessageHandler#onMessage(ByteBuffer)
	 */
	private void metadata(ByteBuffer buffer) throws NetException {
		this.metadataMessageHandler.onMessage(buffer);
	}
	
	/**
	 * <p>发送holepunch消息-rendezvous</p>
	 * 
	 * @param peerSession Peer信息
	 * 
	 * @see HolepunchMessageHnadler#rendezvous(PeerSession)
	 */
	public void holepunchRendezvous(PeerSession peerSession) {
		if(this.holepunchMessageHnadler.supportExtensionType()) {
			this.holepunchMessageHnadler.rendezvous(peerSession);
		}
	}
	
	/**
	 * <p>发送holepunch消息-connect</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 * 
	 * @see HolepunchMessageHnadler#connect(String, int)
	 */
	public void holepunchConnect(String host, Integer port) {
		if(this.holepunchMessageHnadler.supportExtensionType()) {
			this.holepunchMessageHnadler.connect(host, port);
		}
	}
	
	/**
	 * <p>处理holepunch消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see HolepunchMessageHnadler#onMessage(ByteBuffer)
	 */
	private void holepunch(ByteBuffer buffer) throws NetException {
		this.holepunchMessageHnadler.onMessage(buffer);
	}

	/**
	 * <p>发送uploadOnly消息</p>
	 * 
	 * @see UploadOnlyExtensionMessageHandler#uploadOnly()
	 */
	public void uploadOnly() {
		if(this.uploadOnlyExtensionMessageHandler.supportExtensionType()) {
			this.uploadOnlyExtensionMessageHandler.uploadOnly();
		}
	}
	
	/**
	 * <p>处理uploadOnly消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see UploadOnlyExtensionMessageHandler#onMessage(ByteBuffer)
	 */
	private void uploadOnly(ByteBuffer buffer) throws NetException {
		this.uploadOnlyExtensionMessageHandler.onMessage(buffer);
	}
	
	/**
	 * <p>发送dontHave消息</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @see DontHaveExtensionMessageHandler#dontHave(int)
	 */
	public void dontHave(int index) {
		if(this.dontHaveExtensionMessageHandler.supportExtensionType()) {
			this.dontHaveExtensionMessageHandler.dontHave(index);
		}
	}
	
	/**
	 * <p>处理dontHave消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see DontHaveExtensionMessageHandler#onMessage(ByteBuffer)
	 */
	private void dontHave(ByteBuffer buffer) throws NetException {
		this.dontHaveExtensionMessageHandler.onMessage(buffer);
	}
	
	/**
	 * <p>发送扩展消息</p>
	 * 
	 * @param type 扩展消息类型
	 * @param bytes 扩展消息数据
	 */
	public void pushMessage(byte type, byte[] bytes) {
		this.peerSubMessageHandler.pushMessage(PeerConfig.Type.EXTENSION, this.buildMessage(type, bytes));
	}
	
	/**
	 * <p>创建扩展消息</p>
	 * 
	 * @param type 消息类型
	 * @param bytes 消息数据
	 * 
	 * @return 扩展消息
	 */
	private byte[] buildMessage(byte type, byte[] bytes) {
		final byte[] message = new byte[bytes.length + 1];
		message[0] = type; // 扩展消息类型
		System.arraycopy(bytes, 0, message, 1, bytes.length);
		return message;
	}

}

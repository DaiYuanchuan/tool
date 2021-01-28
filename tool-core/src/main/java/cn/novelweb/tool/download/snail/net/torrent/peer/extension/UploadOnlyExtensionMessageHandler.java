package cn.novelweb.tool.download.snail.net.torrent.peer.extension;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.config.PeerConfig;
import cn.novelweb.tool.download.snail.config.PeerConfig.ExtensionType;
import cn.novelweb.tool.download.snail.net.torrent.peer.ExtensionMessageHandler;
import cn.novelweb.tool.download.snail.net.torrent.peer.ExtensionTypeMessageHandler;
import cn.novelweb.tool.download.snail.pojo.session.PeerSession;

/**
 * <p>下载完成时发送uploadOnly消息</p>
 * 
 * @author acgist
 */
public final class UploadOnlyExtensionMessageHandler extends ExtensionTypeMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UploadOnlyExtensionMessageHandler.class);
	
	/**
	 * <p>只上传不下载：{@value}</p>
	 */
	private static final byte UPLOAD_ONLY = 0x01;
	
	/**
	 * @param peerSession Peer信息
	 * @param extensionMessageHandler 扩展消息代理
	 */
	private UploadOnlyExtensionMessageHandler(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		super(ExtensionType.UPLOAD_ONLY, peerSession, extensionMessageHandler);
	}

	/**
	 * <p>创建uploadOnly扩展协议代理</p>
	 * 
	 * @param peerSession Peer信息
	 * @param extensionMessageHandler 扩展消息代理
	 * 
	 * @return uploadOnly扩展协议代理
	 */
	public static UploadOnlyExtensionMessageHandler newInstance(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		return new UploadOnlyExtensionMessageHandler(peerSession, extensionMessageHandler);
	}
	
	@Override
	public void doMessage(ByteBuffer buffer) {
		this.uploadOnly(buffer);
	}

	/**
	 * <p>发送uploadOnly消息</p>
	 */
	public void uploadOnly() {
		LOGGER.debug("发送uploadOnly消息");
		final byte[] bytes = new byte[] { UPLOAD_ONLY }; // 只上传不下载
		this.pushMessage(bytes);
	}
	
	/**
	 * <p>处理uploadOnly消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void uploadOnly(ByteBuffer buffer) {
		final byte value = buffer.get();
		LOGGER.debug("处理uploadOnly消息：{}", value);
		if(value == UPLOAD_ONLY) {
			this.peerSession.flags(PeerConfig.PEX_UPLOAD_ONLY);
		} else {
			this.peerSession.flagsOff(PeerConfig.PEX_UPLOAD_ONLY);
		}
	}

}

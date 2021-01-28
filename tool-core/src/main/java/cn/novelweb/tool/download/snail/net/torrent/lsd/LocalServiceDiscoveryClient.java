package cn.novelweb.tool.download.snail.net.torrent.lsd;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.net.UdpClient;
import cn.novelweb.tool.download.snail.net.torrent.peer.PeerService;
import cn.novelweb.tool.download.snail.pojo.wrapper.HeaderWrapper;
import cn.novelweb.tool.download.snail.utils.ArrayUtils;
import cn.novelweb.tool.download.snail.utils.NetUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;

/**
 * <p>本地发现客户端</p>
 * 
 * @author acgist
 */
public final class LocalServiceDiscoveryClient extends UdpClient<LocalServiceDiscoveryMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceDiscoveryClient.class);
	
	/**
	 * <p>BT-SEARCH协议：{@value}</p>
	 */
	private static final String PROTOCOL = "BT-SEARCH * HTTP/1.1";
	
	/**
	 * @param socketAddress 地址
	 */
	private LocalServiceDiscoveryClient(InetSocketAddress socketAddress) {
		super("LSD Client", new LocalServiceDiscoveryMessageHandler(), socketAddress);
	}

	/**
	 * <p>创建本地发现客户端</p>
	 */
	public static LocalServiceDiscoveryClient newInstance() {
		return new LocalServiceDiscoveryClient(NetUtils.buildSocketAddress(LocalServiceDiscoveryServer.LSD_HOST, LocalServiceDiscoveryServer.LSD_PORT));
	}

	@Override
	public boolean open() {
		return this.open(LocalServiceDiscoveryServer.getInstance().channel());
	}
	
	/**
	 * <p>发送本地发现消息</p>
	 * 
	 * @param infoHashs InfoHash数组
	 */
	public void localSearch(String ... infoHashs) {
		if(ArrayUtils.isEmpty(infoHashs)) {
			return;
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("发送本地发现消息（InfoHash）：{}", String.join(",", infoHashs));
		}
		try {
			this.send(this.buildMessage(infoHashs));
		} catch (NetException e) {
			LOGGER.error("发送本地发现消息异常", e);
		}
	}
	
	/**
	 * <p>创建本地发现消息</p>
	 * 
	 * @param infoHashs InfoHash数组
	 * 
	 * @return 本地发现消息
	 */
	private String buildMessage(String ... infoHashs) {
		final String peerId = StringUtils.hex(PeerService.getInstance().peerId());
		final HeaderWrapper builder = HeaderWrapper.newBuilder(PROTOCOL);
		builder
			.header(LocalServiceDiscoveryMessageHandler.HEADER_HOST, LocalServiceDiscoveryServer.LSD_HOST + ":" + LocalServiceDiscoveryServer.LSD_PORT)
			.header(LocalServiceDiscoveryMessageHandler.HEADER_PORT, String.valueOf(SystemConfig.getTorrentPort()))
			.header(LocalServiceDiscoveryMessageHandler.HEADER_COOKIE, peerId);
		for (String infoHash : infoHashs) {
			builder.header(LocalServiceDiscoveryMessageHandler.HEADER_INFOHASH, infoHash);
		}
		return builder.build();
	}
	
}

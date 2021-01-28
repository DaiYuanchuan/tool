package cn.novelweb.tool.download.snail.net.torrent.lsd;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.config.PeerConfig;
import cn.novelweb.tool.download.snail.context.PeerContext;
import cn.novelweb.tool.download.snail.context.TorrentContext;
import cn.novelweb.tool.download.snail.net.UdpMessageHandler;
import cn.novelweb.tool.download.snail.net.codec.IMessageCodec;
import cn.novelweb.tool.download.snail.net.codec.StringMessageCodec;
import cn.novelweb.tool.download.snail.net.torrent.peer.PeerService;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.pojo.wrapper.HeaderWrapper;
import cn.novelweb.tool.download.snail.utils.ArrayUtils;
import cn.novelweb.tool.download.snail.utils.CollectionUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;

/**
 * <p>本地发现消息代理</p>
 * <p>Local Service Discovery</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0014.html</p>
 * 
 * @author acgist
 */
public final class LocalServiceDiscoveryMessageHandler extends UdpMessageHandler implements IMessageCodec<String> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceDiscoveryMessageHandler.class);

	/**
	 * <p>地址：{@value}</p>
	 */
	public static String HEADER_HOST = "Host";
	/**
	 * <p>端口：{@value}</p>
	 */
	public static String HEADER_PORT = "Port";
	/**
	 * <p>Cookie：{@value}</p>
	 * <p>区别软件本身消息</p>
	 */
	public static String HEADER_COOKIE = "cookie";
	/**
	 * <p>InfoHash：{@value}</p>
	 */
	public static String HEADER_INFOHASH = "Infohash";

	public LocalServiceDiscoveryMessageHandler() {
		this.messageCodec = new StringMessageCodec(this);
	}
	
	@Override
	public void onMessage(String message, InetSocketAddress address) {
		final HeaderWrapper headers = HeaderWrapper.newInstance(message);
		final String host = address.getHostString();
		final String port = headers.header(HEADER_PORT);
		final String cookie = headers.header(HEADER_COOKIE);
		final List<String> infoHashHexs = headers.headerList(HEADER_INFOHASH);
		if(StringUtils.isNumeric(port) && CollectionUtils.isNotEmpty(infoHashHexs)) {
			final byte[] peerId = StringUtils.unhex(cookie);
			if(ArrayUtils.equals(peerId, PeerService.getInstance().peerId())) {
				LOGGER.debug("本地发现消息处理失败：忽略本机");
			} else {
				infoHashHexs.forEach(infoHashHex -> this.doInfoHash(host, port, infoHashHex));
			}
		} else {
			LOGGER.debug("本地发现消息处理失败：{}", message);
		}
	}

	/**
	 * <p>处理本地发现消息</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 * @param infoHashHex InfoHashHex
	 */
	private void doInfoHash(String host, String port, String infoHashHex) {
		final TorrentSession torrentSession = TorrentContext.getInstance().torrentSession(infoHashHex);
		if(torrentSession == null) {
			LOGGER.debug("本地发现消息处理失败（种子信息不存在）：{}", infoHashHex);
		} else {
			LOGGER.debug("本地发现消息：{}-{}-{}", infoHashHex, host, port);
			PeerContext.getInstance().newPeerSession(
				infoHashHex,
				torrentSession.statistics(),
				host,
				Integer.valueOf(port),
				PeerConfig.Source.LSD
			);
		}
	}

}

package cn.novelweb.tool.download.snail.net.torrent.lsd;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.SystemThreadContext;
import cn.novelweb.tool.download.snail.context.TorrentContext;
import cn.novelweb.tool.download.snail.net.UdpServer;

/**
 * <p>本地发现服务端</p>
 * 
 * @author acgist
 */
public final class LocalServiceDiscoveryServer extends UdpServer<LocalServiceDiscoveryAcceptHandler> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceDiscoveryServer.class);
	
	private static final LocalServiceDiscoveryServer INSTANCE = new LocalServiceDiscoveryServer();
	
	public static LocalServiceDiscoveryServer getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>TTL：{@value}</p>
	 */
	private static final int LSD_TTL = 2;
	/**
	 * <p>端口：{@value}</p>
	 */
	public static int LSD_PORT = 6771;
	/**
	 * <p>IPv4组播地址：{@value}</p>
	 */
	public static String LSD_HOST = "239.192.152.143";
	/**
	 * <p>IPv6组播地址：{@value}</p>
	 */
	public static String LSD_HOST_IPV6 = "[ff15::efc0:988f]";
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private LocalServiceDiscoveryServer() {
		super(LSD_PORT, true, "LSD Server", LocalServiceDiscoveryAcceptHandler.getInstance());
		this.join(LSD_TTL, LSD_HOST);
		this.handle();
		this.register();
	}

	/**
	 * <p>注册本地发现服务</p>
	 */
	private void register() {
		LOGGER.debug("注册本地发现服务：定时任务");
		final int interval = SystemConfig.getLsdInterval();
		SystemThreadContext.timerFixedDelay(
			interval,
			interval,
			TimeUnit.SECONDS,
			this::multicast
		);
	}
	
	/**
	 * <p>发送本地发现消息</p>
	 */
	private void multicast() {
		LOGGER.debug("发送本地发现消息");
		final LocalServiceDiscoveryClient client = LocalServiceDiscoveryClient.newInstance();
		TorrentContext.getInstance().allTorrentSession().forEach(session -> {
			if(session.isPrivateTorrent()) {
				LOGGER.debug("私有种子（禁止发送本地发现消息）：{}", session.infoHashHex());
			} else {
				LOGGER.debug("发送本地发现消息：{}", session.infoHashHex());
				client.localSearch(session.infoHashHex());
			}
		});
	}
	
}

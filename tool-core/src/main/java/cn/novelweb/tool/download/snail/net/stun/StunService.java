package cn.novelweb.tool.download.snail.net.stun;

import cn.novelweb.tool.download.snail.config.PeerConfig;
import cn.novelweb.tool.download.snail.config.StunConfig;
import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.NatContext;
import cn.novelweb.tool.download.snail.utils.NetUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * <p>Stun Service</p>
 * 
 * @author acgist
 */
public final class StunService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StunService.class);
	
	private static final StunService INSTANCE = new StunService();
	
	public static StunService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private StunService() {
	}

	/**
	 * <p>端口映射</p>
	 */
	public void mapping() {
		final InetSocketAddress address = this.buildServerAddress();
		if(address == null) {
			LOGGER.warn("STUN服务器配置错误");
			return;
		}
		LOGGER.debug("STUN服务器地址：{}", address);
		StunClient.newInstance(address).mappedAddress();
	}
	
	/**
	 * <p>设置端口映射信息</p>
	 * 
	 * @param externalIpAddress 外网IP地址
	 * @param port 外网端口
	 */
	public void mapping(String externalIpAddress, int port) {
		LOGGER.debug("STUN端口映射：{}-{}", externalIpAddress, port);
		PeerConfig.nat(); // 设置使用NAT穿透
		NatContext.getInstance().stun(); // 设置STUN穿透类型
		SystemConfig.setExternalIpAddress(externalIpAddress);
		SystemConfig.setTorrentPortExt(port);
	}

	/**
	 * <p>获取STUN服务器地址</p>
	 * 
	 * @return STUN服务器地址
	 */
	private InetSocketAddress buildServerAddress() {
		final String server = SystemConfig.getStunServer();
		if(StringUtils.isEmpty(server)) {
			LOGGER.warn("STUN服务器格式错误：{}", server);
			return null;
		}
		final String[] values = server.split(":");
		if(values.length == 1) {
			// 格式：stun1.l.google.com
			if(StringUtils.isNotEmpty(values[0])) {
				return NetUtils.buildSocketAddress(values[0], StunConfig.DEFAULT_PORT);
			}
		} else if(values.length == 2) {
			// 格式：stun1.l.google.com:19302
			if(StringUtils.isNotEmpty(values[0]) && StringUtils.isNumeric(values[1])) {
				return NetUtils.buildSocketAddress(values[0], Integer.parseInt(values[1]));
			}
		} else if(values.length == 3) {
			// 格式：stun:stun1.l.google.com:19302
			if(StringUtils.isNotEmpty(values[1]) && StringUtils.isNumeric(values[2])) {
				return NetUtils.buildSocketAddress(values[1], Integer.parseInt(values[2]));
			}
		}
		LOGGER.warn("STUN服务器格式错误：{}", server);
		return null;
	}
	
}

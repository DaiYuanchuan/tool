package cn.novelweb.tool.download.snail.net.upnp;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.context.NatContext;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.net.UdpMessageHandler;
import cn.novelweb.tool.download.snail.net.codec.IMessageCodec;
import cn.novelweb.tool.download.snail.net.codec.StringMessageCodec;
import cn.novelweb.tool.download.snail.pojo.wrapper.HeaderWrapper;
import cn.novelweb.tool.download.snail.utils.StringUtils;

/**
 * <p>UPNP消息代理</p>
 * <p>协议链接：https://www.rfc-editor.org/rfc/rfc6970.txt</p>
 * <p>协议链接：http://upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v1.0.pdf</p>
 * <p>注：固定IP有时不能正确获取UPNP设置（请设置自动获取IP地址）</p>
 * 
 * @author acgist
 */
public final class UpnpMessageHandler extends UdpMessageHandler implements IMessageCodec<String> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpMessageHandler.class);

	/**
	 * <p>描述文件地址响应头名称：{@value}</p>
	 */
	private static final String HEADER_LOCATION = "location";
	
	public UpnpMessageHandler() {
		this.messageCodec = new StringMessageCodec(this);
	}
	
	@Override
	public void onMessage(String message, InetSocketAddress address) {
		final HeaderWrapper headers = HeaderWrapper.newInstance(message);
		// 判断是否支持UPNP设置
		final boolean support = headers.allHeaders().values().stream()
			.anyMatch(list -> list.stream()
				.anyMatch(value -> StringUtils.startsWith(value, UpnpServer.UPNP_ROOT_DEVICE))
			);
		if(!support) {
			LOGGER.warn("UPNP设置失败（不支持的驱动）：{}", message);
			return;
		}
		final String location = headers.header(HEADER_LOCATION);
		final UpnpService upnpService = UpnpService.getInstance();
		try {
			if(StringUtils.isNotEmpty(location)) {
				upnpService.load(location).mapping();
			} else {
				LOGGER.debug("UPNP没有描述文件地址：{}", message);
			}
		} catch (NetException e) {
			LOGGER.error("UPNP端口映射异常：{}", location, e);
		} finally {
			if(upnpService.useable()) {
				NatContext.getInstance().unlockUpnp();
			}
		}
	}
	
}

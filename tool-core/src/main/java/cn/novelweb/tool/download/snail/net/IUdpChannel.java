package cn.novelweb.tool.download.snail.net;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;

import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.utils.IoUtils;
import cn.novelweb.tool.download.snail.utils.NetUtils;

/**
 * <p>UDP通道接口</p>
 * 
 * @author acgist
 */
public interface IUdpChannel {
	
	/**
	 * <p>随机端口：{@value}</p>
	 */
	int PORT_AUTO = -1;
	/**
	 * <p>本机地址</p>
	 */
	String ADDR_LOCAL = null;
	/**
	 * <p>重用地址：{@value}</p>
	 */
	boolean ADDR_REUSE = true;
	/**
	 * <p>不重用地址：{@value}</p>
	 */
	boolean ADDR_UNREUSE = false;
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>通道属性：随机端口、本机地址、不重用地址</p>
	 * 
	 * @return UDP通道
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see #buildUdpChannel(int, String, boolean)
	 */
	default DatagramChannel buildUdpChannel() throws NetException {
		return this.buildUdpChannel(PORT_AUTO, ADDR_LOCAL, ADDR_UNREUSE);
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>通道属性：本机地址、不重用地址</p>
	 * 
	 * @param port 端口
	 * 
	 * @return UDP通道
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see #buildUdpChannel(int, String, boolean)
	 */
	default DatagramChannel buildUdpChannel(int port) throws NetException {
		return this.buildUdpChannel(port, ADDR_LOCAL, ADDR_UNREUSE);
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>通道属性：不重用地址</p>
	 * 
	 * @param port 端口
	 * @param host 地址
	 * 
	 * @return UDP通道
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see #buildUdpChannel(int, String, boolean)
	 */
	default DatagramChannel buildUdpChannel(int port, String host) throws NetException {
		return this.buildUdpChannel(port, host, ADDR_UNREUSE);
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>通道属性：本机地址</p>
	 * 
	 * @param port 端口
	 * @param reuse 是否重用地址
	 * 
	 * @return UDP通道
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see #buildUdpChannel(int, String, boolean)
	 */
	default DatagramChannel buildUdpChannel(int port, boolean reuse) throws NetException {
		return this.buildUdpChannel(port, ADDR_LOCAL, reuse);
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * 
	 * @param port 端口：{@linkplain #PORT_AUTO 随机端口}
	 * @param host 地址：{@linkplain #ADDR_LOCAL 本机地址}
	 * @param reuse 是否重用地址：{@linkplain #ADDR_REUSE 重用}、{@linkplain #ADDR_UNREUSE 不重用}
	 * 
	 * @return UDP通道
	 * 
	 * @throws NetException 网络异常
	 */
	default DatagramChannel buildUdpChannel(int port, String host, boolean reuse) throws NetException {
		boolean success = true;
		DatagramChannel channel = null;
		try {
//			channel = DatagramChannel.open();
			// TODO：IPv6
			channel = DatagramChannel.open(StandardProtocolFamily.INET); // IPv4
			channel.configureBlocking(false); // 不阻塞
			if(reuse) {
				channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			}
			if(port >= 0) {
				channel.bind(NetUtils.buildSocketAddress(host, port)); // 绑定：使用receive、send方法
//				channel.connect(NetUtils.buildSocketAddress(host, port)); // 连接：使用read、write方法
			}
		} catch (IOException e) {
			success = false;
			throw new NetException("创建UDP通道失败", e);
		} finally {
			if(success) {
				// 成功
			} else {
				IoUtils.close(channel);
				channel = null;
			}
		}
		return channel;
	}
	
}

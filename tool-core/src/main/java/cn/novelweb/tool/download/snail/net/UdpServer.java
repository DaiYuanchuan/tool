package cn.novelweb.tool.download.snail.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.SystemThreadContext;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.utils.IoUtils;
import cn.novelweb.tool.download.snail.utils.NetUtils;

/**
 * <p>UDP服务端</p>
 * <p>全部使用单例：初始化时立即开始监听（客户端和服务端使用同一个通道）</p>
 * 
 * @author acgist
 */
public abstract class UdpServer<T extends UdpAcceptHandler> implements IUdpChannel {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);
	
	/**
	 * <p>UDP服务端消息处理器线程</p>
	 */
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_UDP_SERVER);
	}
	
	/**
	 * <p>服务端名称</p>
	 */
	private String name;
	/**
	 * <p>消息代理</p>
	 */
	private final T handler;
	/**
	 * <p>Selector：每个服务端独立</p>
	 */
	private final Selector selector;
	/**
	 * <p>UDP通道</p>
	 */
	protected final DatagramChannel channel;

	/**
	 * <p>UDP服务端</p>
	 * <p>通道属性：随机端口、本地地址、不重用地址</p>
	 * 
	 * @param name 服务端名称
	 * @param handler 消息代理
	 */
	protected UdpServer(String name, T handler) {
		this(PORT_AUTO, ADDR_LOCAL, ADDR_UNREUSE, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * <p>通道属性：本地地址、不重用地址</p>
	 * 
	 * @param port 端口
	 * @param name 服务端名称
	 * @param handler 消息代理
	 */
	protected UdpServer(int port, String name, T handler) {
		this(port, ADDR_LOCAL, ADDR_UNREUSE, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * <p>通道属性：不重用地址</p>
	 * 
	 * @param port 端口
	 * @param host 地址
	 * @param name 服务端名称
	 * @param handler 消息代理
	 */
	protected UdpServer(int port, String host, String name, T handler) {
		this(port, host, ADDR_UNREUSE, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * <p>通道属性：本地地址</p>
	 * 
	 * @param port 端口
	 * @param reuse 是否重用地址
	 * @param name 服务端名称
	 * @param handler 消息代理
	 */
	protected UdpServer(int port, boolean reuse, String name, T handler) {
		this(port, ADDR_LOCAL, reuse, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * 
	 * @param port 端口
	 * @param host 地址
	 * @param reuse 是否重用地址
	 * @param name 服务端名称
	 * @param handler 消息代理
	 */
	protected UdpServer(int port, String host, boolean reuse, String name, T handler) {
		this.name = name;
		this.handler = handler;
		this.selector = this.buildSelector();
		this.channel = this.buildChannel(port, host, reuse);
	}
	
	/**
	 * <p>创建Selector</p>
	 * 
	 * @return Selector
	 */
	private Selector buildSelector() {
		try {
			return Selector.open();
		} catch (IOException e) {
			LOGGER.error("打开Selector异常：{}", this.name, e);
		}
		return null;
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * 
	 * @param port 端口
	 * @param host 地址
	 * @param reuse 是否重用地址
	 * 
	 * @return UDP通道
	 */
	private DatagramChannel buildChannel(int port, String host, boolean reuse) {
		try {
			return this.buildUdpChannel(port, host, reuse);
		} catch (NetException e) {
			LOGGER.error("打开UDP通道异常：{}", this.name, e);
		}
		return null;
	}
	
	/**
	 * <p>多播（组播）</p>
	 * 
	 * @param ttl TTL
	 * @param group 分组
	 */
	public void join(int ttl, String group) {
		if(this.channel == null) {
			LOGGER.warn("UDP多播失败（通道没有初始化）：{}", this.name);
			return;
		}
		try {
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, ttl);
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
			this.channel.join(InetAddress.getByName(group), NetUtils.DEFAULT_NETWORK_INTERFACE);
		} catch (Exception e) {
			LOGGER.debug("UDP多播异常：{}-{}", this.name, group, e);
		}
	}
	
	/**
	 * <p>消息代理</p>
	 */
	protected void handle() {
		if(this.channel == null) {
			LOGGER.warn("UDP Server通道没有初始化：{}", this.name);
			return;
		}
		if(!this.channel.isOpen()) {
			LOGGER.warn("UDP Server通道已经关闭：{}", this.name);
			return;
		}
		EXECUTOR.submit(() -> this.loopMessage());
	}
	
	/**
	 * <p>消息轮询</p>
	 */
	private void loopMessage() {
		this.selector();
		while (this.channel.isOpen()) {
			this.receive();
		}
		LOGGER.debug("UDP Server退出消息轮询：{}", this.name);
	}
	
	/**
	 * <p>注册Selector消息读取</p>
	 */
	private void selector() {
		try {
			this.channel.register(this.selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			LOGGER.error("UDP Server注册Selector消息读取异常：{}", this.name, e);
		}
	}
	
	/**
	 * <p>接收消息</p>
	 */
	private void receive() {
		try {
			if(this.selector.select() > 0) {
				final Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
				final Iterator<SelectionKey> iterator = selectionKeys.iterator();
				while (iterator.hasNext()) {
					final SelectionKey selectionKey = iterator.next();
					iterator.remove(); // 移除已经取出来的信息
					if (selectionKey.isValid() && selectionKey.isReadable()) {
//						final ByteBuffer buffer = ByteBuffer.allocate(SystemConfig.UDP_BUFFER_LENGTH);
						final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.UDP_BUFFER_LENGTH);
						// 服务器多例：获取不同通道
						// final DatagramChannel channel = (DatagramChannel) selectionKey.channel();
						// 服务端单例：客户端通道=服务端通道
						final InetSocketAddress socketAddress = (InetSocketAddress) this.channel.receive(buffer);
						this.handler.handle(this.channel, buffer, socketAddress);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("UDP Server消息接收异常：{}", this.name, e);
		}
	}
	
	/**
	 * <p>获取UDP通道</p>
	 * 
	 * @return UDP通道
	 */
	public DatagramChannel channel() {
		return this.channel;
	}
	
	/**
	 * <p>关闭UDP Server</p>
	 */
	public void close() {
		LOGGER.debug("关闭UDP Server：{}", this.name);
		IoUtils.close(this.channel);
		IoUtils.close(this.selector);
	}
	
	/**
	 * <p>关闭UDP Server线程池</p>
	 */
	public static final void shutdown() {
		LOGGER.debug("关闭UDP Server线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}

}

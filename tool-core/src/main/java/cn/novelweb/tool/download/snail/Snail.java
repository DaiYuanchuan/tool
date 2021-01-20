package cn.novelweb.tool.download.snail;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.config.DhtConfig;
import cn.novelweb.tool.download.snail.config.TrackerConfig;
import cn.novelweb.tool.download.snail.context.EntityContext;
import cn.novelweb.tool.download.snail.context.NatContext;
import cn.novelweb.tool.download.snail.context.ProtocolContext;
import cn.novelweb.tool.download.snail.context.TaskContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.context.initializer.ConfigInitializer;
import cn.novelweb.tool.download.snail.context.initializer.DhtInitializer;
import cn.novelweb.tool.download.snail.context.initializer.EntityInitializer;
import cn.novelweb.tool.download.snail.context.initializer.Initializer;
import cn.novelweb.tool.download.snail.context.initializer.LocalServiceDiscoveryInitializer;
import cn.novelweb.tool.download.snail.context.initializer.NatInitializer;
import cn.novelweb.tool.download.snail.context.initializer.TaskInitializer;
import cn.novelweb.tool.download.snail.context.initializer.TorrentInitializer;
import cn.novelweb.tool.download.snail.context.initializer.TrackerInitializer;
import cn.novelweb.tool.download.snail.net.application.ApplicationClient;
import cn.novelweb.tool.download.snail.net.application.ApplicationServer;
import cn.novelweb.tool.download.snail.net.torrent.TorrentServer;
import cn.novelweb.tool.download.snail.net.torrent.lsd.LocalServiceDiscoveryServer;
import cn.novelweb.tool.download.snail.net.torrent.peer.PeerServer;
import cn.novelweb.tool.download.snail.net.torrent.tracker.TrackerServer;
import cn.novelweb.tool.download.snail.net.torrent.utp.UtpRequestQueue;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.protocol.Protocol;
import cn.novelweb.tool.download.snail.protocol.ftp.FtpProtocol;
import cn.novelweb.tool.download.snail.protocol.hls.HlsProtocol;
import cn.novelweb.tool.download.snail.protocol.http.HttpProtocol;
import cn.novelweb.tool.download.snail.protocol.magnet.MagnetProtocol;
import cn.novelweb.tool.download.snail.protocol.thunder.ThunderProtocol;
import cn.novelweb.tool.download.snail.protocol.torrent.TorrentProtocol;

/**
 * <p>Snail下载工具</p>
 * <p>快速创建下载任务</p>
 * 
 * @author acgist
 */
public final class Snail {

	private static final Logger LOGGER = LoggerFactory.getLogger(Snail.class);
	
	private static final Snail INSTANCE = new Snail();
	
	public static final Snail getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>是否加下载锁</p>
	 */
	private boolean lock = false;
	/**
	 * <p>是否加载已有任务</p>
	 */
	private boolean buildTask = false;
	/**
	 * <p>是否创建Torrent任务</p>
	 */
	private boolean buildTorrent = false;
	/**
	 * <p>是否启动系统监听</p>
	 * <p>启动检测：开启监听失败表示已经存在系统实例，发送消息唤醒已有实例窗口。</p>
	 */
	private boolean buildApplication = false;
	/**
	 * <p>系统状态</p>
	 */
	private volatile boolean available = false;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private Snail() {
		// 实体优先同步加载
		EntityInitializer.newInstance().sync();
		ConfigInitializer.newInstance().sync();
	}
	
	/**
	 * <p>开始下载</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 下载任务
	 * 
	 * @throws DownloadException 下载异常
	 * 
	 * @see TaskContext#download(String)
	 */
	public ITaskSession download(String url) throws DownloadException {
		return TaskContext.getInstance().download(url);
	}
	
	/**
	 * <p>添加下载锁</p>
	 * <p>任务下载完成解除</p>
	 */
	public void lockDownload() {
		synchronized (this) {
			this.lock = true;
			while(TaskContext.getInstance().allTask().stream().anyMatch(ITaskSession::statusRunning)) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					LOGGER.debug("线程等待异常", e);
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	/**
	 * <p>解除下载锁</p>
	 */
	public void unlockDownload() {
		if(this.lock) {
			synchronized (this) {
				this.notifyAll();
			}
		}
	}
	
	/**
	 * <p>判断系统是否可用</p>
	 * 
	 * @return 是否可用
	 */
	public static final boolean available() {
		return INSTANCE.available;
	}
	
	/**
	 * <p>关闭资源</p>
	 */
	public static final void shutdown() {
		if(INSTANCE.available) {
			INSTANCE.available = false;
			if(INSTANCE.buildApplication) {
				ApplicationServer.getInstance().close();
			}
			// 优先关闭任务
			TaskContext.getInstance().shutdown();
			if(INSTANCE.buildTorrent) {
				PeerServer.getInstance().close();
				TorrentServer.getInstance().close();
				TrackerServer.getInstance().close();
				LocalServiceDiscoveryServer.getInstance().close();
				NatContext.getInstance().shutdown();
				UtpRequestQueue.getInstance().shutdown();
				// 启用BT任务：保存DHT和Tracker配置
				DhtConfig.getInstance().persistent();
				TrackerConfig.getInstance().persistent();
			}
			EntityContext.getInstance().persistent();
		}
	}
	
	/**
	 * <p>SnailBuilder</p>
	 * 
	 * @author acgist
	 */
	public static final class SnailBuilder {
		
		/**
		 * <p>获取SnailBuilder</p>
		 * 
		 * @return SnailBuilder
		 */
		public static final SnailBuilder newBuilder() {
			return new SnailBuilder();
		}
		
		/**
		 * <p>禁止创建实例</p>
		 */
		private SnailBuilder() {
		}

		/**
		 * <p>同步创建Snail</p>
		 * 
		 * @return Snail
		 */
		public Snail buildSync() {
			return this.build(true);
		}
		
		/**
		 * <p>异步创建Snail</p>
		 * 
		 * @return Snail
		 */
		public Snail buildAsyn() {
			return this.build(false);
		}
		
		/**
		 * <p>创建Snail</p>
		 * 
		 * @param sync 是否同步初始化
		 * 
		 * @return Snail
		 * 
		 * @throws DownloadException 下载异常
		 */
		public synchronized Snail build(boolean sync) {
			if(INSTANCE.available) {
				return INSTANCE;
			}
			INSTANCE.available = true;
			if(INSTANCE.buildApplication) {
				INSTANCE.available = ApplicationServer.getInstance().listen();
			}
			if(INSTANCE.available) {
				ProtocolContext.getInstance().available(INSTANCE.available);
				this.buildInitializers().forEach(initializer -> {
					if(sync) {
						initializer.sync();
					} else {
						initializer.asyn();
					}
				});
			} else {
				LOGGER.debug("已有系统实例：唤醒实例窗口");
				ApplicationClient.notifyWindow();
			}
			return INSTANCE;
		}

		/**
		 * <p>加载初始化列表</p>
		 * 
		 * @return 初始化列表
		 */
		private List<Initializer> buildInitializers() {
			final List<Initializer> list = new ArrayList<>();
			if(INSTANCE.buildTorrent) {
				list.add(NatInitializer.newInstance());
				list.add(DhtInitializer.newInstance());
				list.add(TorrentInitializer.newInstance());
				list.add(TrackerInitializer.newInstance());
				list.add(LocalServiceDiscoveryInitializer.newInstance());
			}
			if(INSTANCE.buildTask) {
				list.add(TaskInitializer.newInstance());
			}
			return list;
		}
		
		/**
		 * <p>加载已有任务</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder loadTask() {
			INSTANCE.buildTask = true;
			return this;
		}

		/**
		 * <p>启动系统监听</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder application() {
			INSTANCE.buildApplication = true;
			return this;
		}
		
		/**
		 * <p>注册下载协议</p>
		 * 
		 * @param protocol 下载协议
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder register(Protocol protocol) {
			ProtocolContext.getInstance().register(protocol);
			return this;
		}
		
		/**
		 * <p>注册FTP下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableFtp() {
			return this.register(FtpProtocol.getInstance());
		}
		
		/**
		 * <p>注册HLS下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableHls() {
			return this.register(HlsProtocol.getInstance());
		}
		
		/**
		 * <p>注册HTTP下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableHttp() {
			return this.register(HttpProtocol.getInstance());
		}
		
		/**
		 * <p>注册Magnet下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableMagnet() {
			INSTANCE.buildTorrent = true;
			return this.register(MagnetProtocol.getInstance());
		}
		
		/**
		 * <p>注册Thunder下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableThunder() {
			return this.register(ThunderProtocol.getInstance());
		}
		
		/**
		 * <p>注册Torrent下载协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableTorrent() {
			INSTANCE.buildTorrent = true;
			return this.register(TorrentProtocol.getInstance());
		}
		
		/**
		 * <p>注册所有协议</p>
		 * 
		 * @return SnailBuilder
		 */
		public SnailBuilder enableAllProtocol() {
			return this
				.enableFtp()
				.enableHls()
				.enableHttp()
				.enableMagnet()
				.enableThunder()
				.enableTorrent();
		}

	}
	
}

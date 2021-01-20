package cn.novelweb.tool.download.snail.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.novelweb.tool.download.snail.IContext;
import cn.novelweb.tool.download.snail.Snail;
import cn.novelweb.tool.download.snail.Snail.SnailBuilder;
import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.format.JSON;
import cn.novelweb.tool.download.snail.logger.LoggerContext;
import cn.novelweb.tool.download.snail.net.TcpClient;
import cn.novelweb.tool.download.snail.net.TcpServer;
import cn.novelweb.tool.download.snail.net.UdpServer;
import cn.novelweb.tool.download.snail.net.http.HttpClient;
import cn.novelweb.tool.download.snail.utils.FileUtils;

/**
 * <p>系统上下文</p>
 * 
 * @author acgist
 */
public final class SystemContext implements IContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemContext.class);

	private static final SystemContext INSTANCE = new SystemContext();
	
	public static final SystemContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>系统类型</p>
	 * 
	 * @author acgist
	 */
	public enum SystemType {
		
		/**
		 * <p>Mac</p>
		 */
		MAC("Mac OS", "Mac OS X"),
		/**
		 * <p>Linux</p>
		 */
		LINUX("Linux"),
		/**
		 * <p>Windows</p>
		 */
		WINDOWS("Windows XP", "Windows Vista", "Windows 7", "Windows 10"),
		/**
		 * <p>Android</p>
		 */
		ANDROID("Android");
		
		/**
		 * <p>系统名称</p>
		 */
		private final String[] osNames;

		/**
		 * @param osNames 系统名称
		 */
		private SystemType(String ... osNames) {
			this.osNames = osNames;
		}

		/**
		 * <p>获取当前系统类型</p>
		 * 
		 * @return 当前系统类型
		 */
		public static final SystemType local() {
			final String osName = SystemContext.osName();
			for (SystemType type : SystemType.values()) {
				for (String value : type.osNames) {
					if(value.equals(osName)) {
						return type;
					}
				}
			}
			LOGGER.warn("未知系统：{}", osName);
			return null;
		}
		
	}
	
	/**
	 * <p>系统名称</p>
	 */
	private final String osName;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private SystemContext() {
		this.osName = System.getProperty("os.name");
	}
	
	/**
	 * <p>系统信息</p>
	 */
	public static final void info() {
		final Runtime runtime = Runtime.getRuntime();
		LOGGER.info("操作系统名称：{}", System.getProperty("os.name"));
		LOGGER.info("操作系统架构：{}", System.getProperty("os.arch"));
		LOGGER.info("操作系统版本：{}", System.getProperty("os.version"));
		LOGGER.info("操作系统可用处理器数量：{}", runtime.availableProcessors());
		LOGGER.info("Java版本：{}", System.getProperty("java.version"));
		LOGGER.info("Java主目录：{}", System.getProperty("java.home"));
		LOGGER.info("Java库目录：{}", System.getProperty("java.library.path"));
		LOGGER.info("虚拟机名称：{}", System.getProperty("java.vm.name"));
		final String freeMemory = FileUtils.formatSize(runtime.freeMemory());
		final String totalMemory = FileUtils.formatSize(runtime.totalMemory());
		final String maxMemory = FileUtils.formatSize(runtime.maxMemory());
		LOGGER.info("虚拟机空闲内存：{}", freeMemory);
		LOGGER.info("虚拟机已用内存：{}", totalMemory);
		LOGGER.info("虚拟机最大内存：{}", maxMemory);
		LOGGER.info("用户目录：{}", System.getProperty("user.home"));
		LOGGER.info("工作目录：{}", System.getProperty("user.dir"));
		LOGGER.info("文件编码：{}", System.getProperty("file.encoding"));
	}
	
	/**
	 * <p>系统初始化</p>
	 * 
	 * @return Snail
	 */
	public static final Snail build() {
		LOGGER.info("系统初始化");
		return SnailBuilder.newBuilder()
			.loadTask()
			.application()
			.enableAllProtocol()
			.buildAsyn();
	}
	
	/**
	 * <p>系统关闭</p>
	 * <p>所有线程都是守护线程，所以可以不用手动关闭。</p>
	 * 
	 * @see SystemThreadContext
	 */
	public static final void shutdown() {
		if(Snail.available()) {
			SystemThreadContext.submit(() -> {
				LOGGER.info("系统关闭中...");
				GuiContext.getInstance().hide();
				Snail.shutdown();
				TcpClient.shutdown();
				TcpServer.shutdown();
				UdpServer.shutdown();
				GuiContext.getInstance().exit();
				SystemThreadContext.shutdown();
				LOGGER.info("系统已关闭");
				LoggerContext.shutdown();
			});
		} else {
			GuiContext.getInstance().alert("关闭提示", "系统正在关闭中...");
		}
	}
	
	/**
	 * <p>获取系统名称</p>
	 * 
	 * @return 系统名称
	 */
	public static final String osName() {
		return INSTANCE.osName;
	}

	/**
	 * <p>判断是不是最新版本</p>
	 * 
	 * @return 是不是最新版本
	 */
	public static final boolean latestRelease() {
		try {
			// 本地版本：1.0.0
			final String version = SystemConfig.getVersion();
			final String body = HttpClient
				.newInstance(SystemConfig.getLatestRelease())
				.get()
				.responseToString();
			final JSON json = JSON.ofString(body);
			// 最新版本：v1.0.0
			final String latestVersion = json.getString("tag_name");
			LOGGER.debug("版本信息：{}-{}", version, latestVersion);
			return latestVersion.substring(1).equals(version);
		} catch (NetException e) {
			LOGGER.error("获取版本信息异常", e);
		}
		return true;
	}
	
}

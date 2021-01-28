package cn.novelweb.tool.download.snail.protocol;

import java.io.File;

import cn.novelweb.tool.download.snail.config.DownloadConfig;
import cn.novelweb.tool.download.snail.context.EntityContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.downloader.IDownloader;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.ITaskSessionStatus.Status;
import cn.novelweb.tool.download.snail.pojo.entity.TaskEntity;
import cn.novelweb.tool.download.snail.pojo.session.TaskSession;
import cn.novelweb.tool.download.snail.utils.FileUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;

/***
 * <p>下载协议</p>
 * 
 * TODO：RTP RTCP RTMP RTSP
 * TODO：blob:http://
 * 
 * @author acgist
 */
public abstract class Protocol {
	
	/**
	 * <p>磁力链接（标准）：{@value}</p>
	 */
	private static final String MAGNET_BASIC = "magnet:\\?.+";
	/**
	 * <p>磁力链接（32位Hash）：{@value}</p>
	 */
	private static final String MAGNET_HASH_32 = "[a-zA-Z0-9]{32}";
	/**
	 * <p>磁力链接（40位Hash）：{@value}</p>
	 */
	private static final String MAGNET_HASH_40 = "[a-zA-Z0-9]{40}";
	
	/**
	 * <p>协议类型</p>
	 * 
	 * @author acgist
	 */
	public enum Type {

		/** udp */
		UDP(
			new String[] {"udp://.+"},
			new String[] {"udp://"},
			new String[] {},
			"udp://",
			""
		),
		/** tcp */
		TCP(
			new String[] {"tcp://.+"},
			new String[] {"tcp://"},
			new String[] {},
			"tcp://",
			""
		),
		/** ftp */
		FTP(
			new String[] {"ftp://.+"},
			new String[] {"ftp://"},
			new String[] {},
			"ftp://",
			""
		),
		/** hls */
		HLS(
			new String[] {"http://.+\\.m3u8", "https://.+\\.m3u8"},
			new String[] {},
			new String[] {".m3u8"},
			"",
			".m3u8"
		),
		/** http、https */
		HTTP(
			new String[] {"http://.+", "https://.+"},
			new String[] {"http://", "https://"},
			new String[] {},
			"http://",
			""
		),
		/** 磁力链接 */
		MAGNET(
			new String[] {MAGNET_BASIC, MAGNET_HASH_32, MAGNET_HASH_40},
			new String[] {"magnet:?xt=urn:btih:"},
			new String[] {},
			"magnet:?xt=urn:btih:",
			""
		),
		/** 迅雷链接 */
		THUNDER(
			new String[] {"thunder://.+"},
			new String[] {"thunder://"},
			new String[] {},
			"thunder://",
			""
		),
		/** BT：BitTorrent */
		TORRENT(
			new String[] {".+\\.torrent"},
			new String[] {},
			new String[] {".torrent"},
			"",
			".torrent"
		);
		
		/**
		 * <p>正则表达式</p>
		 */
		private final String[] regexs;
		/**
		 * <p>前缀</p>
		 */
		private final String[] prefix;
		/**
		 * <p>后缀</p>
		 */
		private final String[] suffix;
		/**
		 * <p>默认前缀</p>
		 */
		private final String defaultPrefix;
		/**
		 * <p>默认后缀</p>
		 */
		private final String defaultSuffix;
		
		/**
		 * @param regexs 正则表达式
		 * @param prefix 前缀
		 * @param suffix 后缀
		 * @param defaultPrefix 默认前缀
		 * @param defaultSuffix 默认后缀
		 */
		private Type(String[] regexs, String[] prefix, String[] suffix, String defaultPrefix, String defaultSuffix) {
			this.regexs = regexs;
			this.prefix = prefix;
			this.suffix = suffix;
			this.defaultPrefix = defaultPrefix;
			this.defaultSuffix = defaultSuffix;
		}

		/**
		 * @return 正则表达式
		 */
		public String[] regexs() {
			return this.regexs;
		}

		/**
		 * @return 前缀
		 */
		public String[] prefix() {
			return this.prefix;
		}
		
		/**
		 * <p>获取链接的前缀</p>
		 * 
		 * @param url 链接
		 * 
		 * @return 前缀
		 */
		public String prefix(String url) {
			for (String value : this.prefix) {
				if(StringUtils.startsWith(url, value)) {
					return value;
				}
			}
			return null;
		}
		
		/**
		 * @return 后缀
		 */
		public String[] suffix() {
			return this.suffix;
		}
		
		/**
		 * <p>获取链接的后缀</p>
		 * 
		 * @param url 链接
		 * 
		 * @return 后缀
		 */
		public String suffix(String url) {
			for (String value : this.suffix) {
				if(StringUtils.endsWith(url, value)) {
					return value;
				}
			}
			return null;
		}
		
		/**
		 * @return 默认前缀
		 */
		public String defaultPrefix() {
			return this.defaultPrefix;
		}
		
		/**
		 * @return 默认后缀
		 */
		public String defaultSuffix() {
			return this.defaultSuffix;
		}
		
		/**
		 * <p>判断协议是否支持下载链接</p>
		 * 
		 * @param url 链接
		 * 
		 * @return true-匹配；false-不匹配；
		 */
		public boolean verify(String url) {
			for (String regex : this.regexs) {
				final boolean match = StringUtils.regex(url, regex, true);
				if(match) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * <p>将Hash转为完整磁力链接</p>
		 * 
		 * @param hash 磁力链接Hash
		 * 
		 * @return 完整磁力链接
		 */
		public static String buildMagnet(String hash) {
			if(verifyMagnet(hash)) {
				return hash;
			}
			return Type.MAGNET.defaultPrefix + hash.toLowerCase();
		}
		
		/**
		 * <p>判断是否是完整磁力链接</p>
		 * 
		 * @param url 磁力链接
		 * 
		 * @return true-是；false-不是；
		 */
		public static boolean verifyMagnet(String url) {
			return StringUtils.regex(url, MAGNET_BASIC, true);
		}
		
		/**
		 * <p>判断是否是32位磁力链接Hash</p>
		 * 
		 * @param url 磁力链接
		 * 
		 * @return true-是；false-不是；
		 */
		public static boolean verifyMagnetHash32(String url) {
			return StringUtils.regex(url, MAGNET_HASH_32, true);
		}
		
		/**
		 * <p>判断是否是40位磁力链接Hash</p>
		 * 
		 * @param url 磁力链接
		 * 
		 * @return true-是；false-不是；
		 */
		public static boolean verifyMagnetHash40(String url) {
			return StringUtils.regex(url, MAGNET_HASH_40, true);
		}
		
	}
	
	/**
	 * <p>协议类型</p>
	 */
	protected final Type type;
	/**
	 * <p>下载链接</p>
	 */
	protected String url;
	/**
	 * <p>下载任务</p>
	 */
	protected TaskEntity taskEntity;
	
	/**
	 * @param type 协议类型
	 */
	protected Protocol(Type type) {
		this.type = type;
	}

	/**
	 * <p>获取协议类型</p>
	 * 
	 * @return 协议类型
	 */
	public Type type() {
		return this.type;
	}
	
	/**
	 * <p>获取协议名称</p>
	 * 
	 * @return 协议名称
	 */
	public abstract String name();
	
	/**
	 * <p>判断协议是否支持下载链接</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return true-支持；false-不支持；
	 */
	public boolean verify(String url) {
		if(this.type == null) {
			return false;
		}
		return this.type.verify(url);
	}
	
	/**
	 * <p>是否可用</p>
	 * 
	 * @return 是否可用
	 */
	public abstract boolean available();
	
	/**
	 * <p>创建下载器</p>
	 * 
	 * @param taskSession 下载任务
	 * 
	 * @return 下载器
	 */
	public abstract IDownloader buildDownloader(ITaskSession taskSession);
	
	/**
	 * <p>创建下载任务</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public synchronized ITaskSession buildTaskSession(String url) throws DownloadException {
		this.url = url.trim();
		boolean success = true;
		try {
			this.buildTaskEntity();
			return TaskSession.newInstance(this.taskEntity);
		} catch (DownloadException e) {
			success = false;
			throw e;
		} catch (Exception e) {
			success = false;
			throw new DownloadException("下载失败", e);
		} finally {
			this.release(success);
		}
	}

	/**
	 * <p>创建下载任务</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildTaskEntity() throws DownloadException {
		this.taskEntity = new TaskEntity();
		this.prep();
		this.buildUrl();
		this.buildType();
		this.buildStatus();
		final String fileName = FileUtils.fileNameFormat(this.buildFileName());
		this.buildName(fileName);
		this.buildFile(fileName);
		this.buildFileType(fileName);
		this.buildSize();
		this.done();
		this.persistentTaskEntity();
	}
	
	/**
	 * <p>预处理</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void prep() throws DownloadException {
	}
	
	/**
	 * <p>设置URL</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildUrl() throws DownloadException {
		this.taskEntity.setUrl(this.url);
	}

	/**
	 * <p>设置下载类型</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildType() throws DownloadException {
		this.taskEntity.setType(this.type);
	}

	/**
	 * <p>设置任务状态</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildStatus() throws DownloadException {
		this.taskEntity.setStatus(Status.AWAIT);
	}

	/**
	 * <p>获取文件名称</p>
	 * 
	 * @return 文件名称
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected String buildFileName() throws DownloadException {
		return FileUtils.fileName(this.url);
	}

	/**
	 * <p>设置任务名称</p>
	 * 
	 * @param fileName 文件名称
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildName(String fileName) throws DownloadException {
		String name = fileName;
		// 去掉后缀
		final int index = fileName.lastIndexOf('.');
		if(index != -1) {
			name = fileName.substring(0, index);
		}
		this.taskEntity.setName(name);
	}
	
	/**
	 * <p>设置下载文件、目录</p>
	 * 
	 * @param fileName 文件名称
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildFile(String fileName) throws DownloadException {
		final String filePath = DownloadConfig.getPath(fileName);
		final File file = new File(filePath);
		if(file.exists()) {
			throw new DownloadException("下载文件已经存在：" + file);
		}
		this.taskEntity.setFile(filePath);
	}
	
	/**
	 * <p>设置任务文件类型</p>
	 * 
	 * @param fileName 文件名称
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildFileType(String fileName) throws DownloadException {
		this.taskEntity.setFileType(FileUtils.fileType(fileName));
	}

	/**
	 * <p>设置任务大小</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildSize() throws DownloadException {
	}
	
	/**
	 * <p>完成处理</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void done() throws DownloadException {
	}
	
	/**
	 * <p>持久化任务</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void persistentTaskEntity() throws DownloadException {
		EntityContext.getInstance().save(this.taskEntity);
	}
	
	/**
	 * <p>释放资源</p>
	 * 
	 * @param success 创建状态：true-成功；false-失败；
	 */
	protected void release(boolean success) {
		this.url = null;
		this.taskEntity = null;
	}

}

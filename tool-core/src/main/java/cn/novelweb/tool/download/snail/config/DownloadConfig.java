package cn.novelweb.tool.download.snail.config;

import cn.novelweb.tool.download.snail.context.EntityContext;
import cn.novelweb.tool.download.snail.context.TaskContext;
import cn.novelweb.tool.download.snail.pojo.entity.ConfigEntity;
import cn.novelweb.tool.download.snail.utils.FileUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * <p>下载配置</p>
 * <p>默认加载配置文件配置，如果实体存在相同配置，则使用实体配置覆盖。</p>
 * 
 * @author acgist
 */
public final class DownloadConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadConfig.class);
	
	/**
	 * <p>单例对象</p>
	 */
	private static final DownloadConfig INSTANCE = new DownloadConfig();
	
	/**
	 * <p>获取单例对象</p>
	 * 
	 * @return 单例对象
	 */
	public static DownloadConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>配置文件：{@value}</p>
	 */
	private static final String DOWNLOAD_CONFIG = "/config/download.properties";
	/**
	 * <p>下载速度和上传速度的比例：{@value}</p>
	 * <p>比例={@link #downloadBufferByte}/{@link #uploadBufferByte}</p>
	 */
	private static final int DOWNLOAD_UPLOAD_SCALE = 4;
	/**
	 * <p>下载目录配置名称：{@value}</p>
	 * 
	 * @see #path
	 */
	private static final String DOWNLOAD_PATH = "acgist.download.path";
	/**
	 * <p>下载数量配置名称：{@value}</p>
	 * 
	 * @see #size
	 */
	private static final String DOWNLOAD_SIZE = "acgist.download.size";
	/**
	 * <p>消息提示配置名称：{@value}</p>
	 * 
	 * @see #notice
	 */
	private static final String DOWNLOAD_NOTICE = "acgist.download.notice";
	/**
	 * <p>下载速度（单个）（KB）配置名称：{@value}</p>
	 * 
	 * @see #buffer
	 */
	private static final String DOWNLOAD_BUFFER = "acgist.download.buffer";
	/**
	 * <p>最后一次选择目录配置名称：{@value}</p>
	 * 
	 * @see #lastPath
	 */
	private static final String DOWNLOAD_LAST_PATH = "acgist.download.last.path";
	/**
	 * <p>磁盘缓存（单个）（MB）配置名称：{@value}</p>
	 * 
	 * @see #memoryBuffer
	 */
	private static final String DOWNLOAD_MEMORY_BUFFER = "acgist.download.memory.buffer";
	
	static {
		LOGGER.debug("初始化下载配置：{}", DOWNLOAD_CONFIG);
		INSTANCE.initFromProperties();
		INSTANCE.initFromEntity();
		INSTANCE.refreshUploadDownloadBuffer();
		INSTANCE.refreshMemoryBuffer();
		INSTANCE.logger();
		INSTANCE.release();
	}
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private DownloadConfig() {
		super(DOWNLOAD_CONFIG);
	}
	
	/**
	 * <p>下载目录</p>
	 */
	private String path;
	/**
	 * <p>下载数量</p>
	 */
	private int size;
	/**
	 * <p>消息提示</p>
	 */
	private boolean notice;
	/**
	 * <p>下载速度（单个）（KB）</p>
	 */
	private int buffer;
	/**
	 * <p>最后一次选择目录</p>
	 */
	private String lastPath;
	/**
	 * <p>磁盘缓存（单个）（MB）</p>
	 */
	private int memoryBuffer;
	/**
	 * <p>上传速度（单个）（B）</p>
	 * <p>缓存：防止重复计算</p>
	 */
	private int uploadBufferByte;
	/**
	 * <p>下载速度（单个）（B）</p>
	 * <p>缓存：防止重复计算</p>
	 */
	private int downloadBufferByte;
	/**
	 * <p>磁盘缓存（单个）（B）</p>
	 * <p>缓存：防止重复计算</p>
	 */
	private int memoryBufferByte;
	
	/**
	 * <p>初始化配置：配置文件</p>
	 */
	private void initFromProperties() {
		this.path = this.getString(DOWNLOAD_PATH);
		this.size = this.getInteger(DOWNLOAD_SIZE, 4);
		this.buffer = this.getInteger(DOWNLOAD_BUFFER, 1024);
		this.notice = this.getBoolean(DOWNLOAD_NOTICE, true);
		this.lastPath = this.getString(DOWNLOAD_LAST_PATH);
		this.memoryBuffer = this.getInteger(DOWNLOAD_MEMORY_BUFFER, 8);
	}
	
	/**
	 * <p>初始化配置：实体</p>
	 */
	private void initFromEntity() {
		final EntityContext entityContext = EntityContext.getInstance();
		ConfigEntity entity = null;
		entity = entityContext.findConfigByName(DOWNLOAD_PATH);
		this.path = this.getString(entity, this.path);
		entity = entityContext.findConfigByName(DOWNLOAD_SIZE);
		this.size = this.getInteger(entity, this.size);
		entity = entityContext.findConfigByName(DOWNLOAD_NOTICE);
		this.notice = this.getBoolean(entity, this.notice);
		entity = entityContext.findConfigByName(DOWNLOAD_BUFFER);
		this.buffer = this.getInteger(entity, this.buffer);
		entity = entityContext.findConfigByName(DOWNLOAD_LAST_PATH);
		this.lastPath = this.getString(entity, this.lastPath);
		entity = entityContext.findConfigByName(DOWNLOAD_MEMORY_BUFFER);
		this.memoryBuffer = this.getInteger(entity, this.memoryBuffer);
	}
	
	/**
	 * <p>记录日志</p>
	 */
	private void logger() {
		LOGGER.debug("下载目录：{}", this.path);
		LOGGER.debug("下载数量：{}", this.size);
		LOGGER.debug("消息提示：{}", this.notice);
		LOGGER.debug("下载速度（单个）（KB）：{}", this.buffer);
		LOGGER.debug("最后一次选择目录：{}", this.lastPath);
		LOGGER.debug("磁盘缓存（单个）（MB）：{}", this.memoryBuffer);
	}
	
	/**
	 * <p>设置下载目录路径</p>
	 * 
	 * @param path 下载目录路径
	 */
	public static void setPath(String path) {
		if(StringUtils.equals(INSTANCE.path, path)) {
			// 忽略没有修改
			return;
		}
		INSTANCE.path = path;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_PATH, path);
	}
	
	/**
	 * <p>获取下载目录路径</p>
	 * <p>下载目录存在：返回下载目录路径</p>
	 * <p>下载目录不在：返回{@code user.dir}路径 + 下载目录路径</p>
	 * 
	 * @return 下载目录路径
	 */
	public static String getPath() {
		String path = INSTANCE.path;
		final File file = new File(path);
		if(file.exists()) {
			return path;
		}
		path = SystemConfig.userDir(path);
		FileUtils.buildFolder(path, false);
		return path;
	}

	/**
	 * <p>获取下载目录下文件路径</p>
	 * 
	 * @param fileName 文件名称
	 * 
	 * @return 文件路径
	 */
	public static String getPath(String fileName) {
		if(StringUtils.isEmpty(fileName)) {
			throw new IllegalArgumentException("文件名称格式错误：" + fileName);
		}
		return FileUtils.file(getPath(), fileName);
	}
	
	/**
	 * <p>设置下载数量</p>
	 * 
	 * @param size 下载数量
	 */
	public static void setSize(int size) {
		if(INSTANCE.size == size) {
			// 忽略没有修改
			return;
		}
		INSTANCE.size = size;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_SIZE, String.valueOf(size));
		// 刷新下载任务
		TaskContext.getInstance().refresh();
	}

	/**
	 * <p>获取下载数量</p>
	 * 
	 * @return 下载数量
	 */
	public static int getSize() {
		return INSTANCE.size;
	}
	
	/**
	 * <p>设置消息提示</p>
	 * 
	 * @param notice 是否提示消息
	 */
	public static void setNotice(boolean notice) {
		if(INSTANCE.notice == notice) {
			// 忽略没有修改
			return;
		}
		INSTANCE.notice = notice;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_NOTICE, String.valueOf(notice));
	}

	/**
	 * <p>获取消息提示</p>
	 * 
	 * @return 是否提示消息
	 */
	public static boolean getNotice() {
		return INSTANCE.notice;
	}
	
	/**
	 * <p>设置下载速度（单个）（KB）</p>
	 * 
	 * @param buffer 下载速度
	 */
	public static void setBuffer(int buffer) {
		if(INSTANCE.buffer == buffer) {
			// 忽略没有修改
			return;
		}
		INSTANCE.buffer = buffer;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_BUFFER, String.valueOf(buffer));
		// 刷新下载速度和上传速度
		INSTANCE.refreshUploadDownloadBuffer();
	}
	
	/**
	 * <p>获取下载速度（单个）（KB）</p>
	 * 
	 * @return 下载速度
	 */
	public static int getBuffer() {
		return INSTANCE.buffer;
	}
	
	/**
	 * <p>获取上传速度（单个）（B）</p>
	 * 
	 * @return 上传速度
	 */
	public static int getUploadBufferByte() {
		return INSTANCE.uploadBufferByte;
	}
	
	/**
	 * <p>获取下载速度（单个）（B）</p>
	 * 
	 * @return 下载速度
	 */
	public static int getDownloadBufferByte() {
		return INSTANCE.downloadBufferByte;
	}
	
	/**
	 * <p>刷新下载速度和上传速度</p>
	 */
	private void refreshUploadDownloadBuffer() {
		this.downloadBufferByte = this.buffer * SystemConfig.ONE_KB;
		this.uploadBufferByte = this.downloadBufferByte / DOWNLOAD_UPLOAD_SCALE;
	}
	
	/**
	 * <p>设置最后一次选择目录</p>
	 * 
	 * @param lastPath 最后一次选择目录
	 */
	public static void setLastPath(String lastPath) {
		if(StringUtils.equals(INSTANCE.lastPath, lastPath)) {
			// 忽略没有修改
			return;
		}
		INSTANCE.lastPath = lastPath;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_LAST_PATH, lastPath);
	}
	
	/**
	 * <p>获取最后一次选择目录</p>
	 * <p>如果最后一次选择目录为空返回下载目录</p>
	 * 
	 * @return 最后一次选择目录
	 */
	public static String getLastPath() {
		if(StringUtils.isEmpty(INSTANCE.lastPath)) {
			return getPath();
		} else {
			return INSTANCE.lastPath;
		}
	}
	
	/**
	 * <p>获取最后一次选择目录文件</p>
	 * 
	 * @return 最后一次选择目录文件
	 */
	public static File getLastPathFile() {
		return new File(getLastPath());
	}
	
	/**
	 * <p>设置磁盘缓存（单个）（MB）</p>
	 * 
	 * @param memoryBuffer 磁盘缓存
	 */
	public static void setMemoryBuffer(int memoryBuffer) {
		if(INSTANCE.memoryBuffer == memoryBuffer) {
			// 忽略没有修改
			return;
		}
		INSTANCE.memoryBuffer = memoryBuffer;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_MEMORY_BUFFER, String.valueOf(memoryBuffer));
		// 刷新磁盘缓存
		INSTANCE.refreshMemoryBuffer();
	}

	/**
	 * <p>获取磁盘缓存（单个）（MB）</p>
	 * 
	 * @return 磁盘缓存
	 */
	public static int getMemoryBuffer() {
		return INSTANCE.memoryBuffer;
	}

	/**
	 * <p>获取磁盘缓存（单个）（B）</p>
	 * 
	 * @return 磁盘缓存
	 */
	public static int getMemoryBufferByte() {
		return INSTANCE.memoryBufferByte;
	}
	
	/**
	 * <p>获取磁盘缓存（单个）（B）</p>
	 * <p>如果文件小于默认磁盘缓存直接使用文件大小作为缓存大小</p>
	 * 
	 * @param fileSize 文件大小
	 * 
	 * @return 磁盘缓存
	 */
	public static int getMemoryBufferByte(final long fileSize) {
		final int bufferSize = getMemoryBufferByte();
		if(fileSize > 0L) {
			if(bufferSize > fileSize) {
				return (int) fileSize;
			} else {
				return bufferSize;
			}
		}
		return bufferSize;
	}
	
	/**
	 * <p>刷新磁盘缓存</p>
	 */
	private void refreshMemoryBuffer() {
		this.memoryBufferByte = this.memoryBuffer * SystemConfig.ONE_MB;
	}
	
}

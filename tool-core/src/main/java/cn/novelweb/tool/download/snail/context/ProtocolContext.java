package cn.novelweb.tool.download.snail.context;

import cn.novelweb.tool.download.snail.IContext;
import cn.novelweb.tool.download.snail.Snail;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.downloader.IDownloader;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.protocol.Protocol;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>下载协议上下文</p>
 * 
 * @author acgist
 */
public final class ProtocolContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolContext.class);

	private static final ProtocolContext INSTANCE = new ProtocolContext();
	
	public static final ProtocolContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>下载协议</p>
	 * <p>不添加同步锁：协议注册完成后调用{@link #available(boolean)}设置可用</p>
	 */
	private final List<Protocol> protocols;
	/**
	 * <p>可用锁：协议没有加载完成时阻塞所有获取协议的线程</p>
	 */
	private final AtomicBoolean availableLock = new AtomicBoolean(false);
	
	private ProtocolContext() {
		this.protocols = new ArrayList<>();
	}
	
	/**
	 * <p>注册协议</p>
	 * 
	 * @param protocol 协议
	 * 
	 * @return ProtocolContext
	 */
	public ProtocolContext register(Protocol protocol) {
		if(this.protocols.contains(protocol)) {
			LOGGER.debug("下载协议已经注册：{}", protocol.name());
		} else {
			LOGGER.info("注册下载协议：{}", protocol.name());
			this.protocols.add(protocol);
		}
		return this;
	}
	
	/**
	 * <p>创建下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	public IDownloader buildDownloader(ITaskSession taskSession) throws DownloadException {
		final Protocol.Type type = taskSession.getType();
		final Optional<Protocol> optional = this.protocols.stream()
			.filter(Protocol::available)
			.filter(protocol -> protocol.type() == type)
			.findFirst();
		if(!optional.isPresent()) {
			throw new DownloadException("不支持的下载类型：" + type);
		}
		final IDownloader downloader = optional.get().buildDownloader(taskSession);
		if(downloader == null) {
			throw new DownloadException("不支持的下载类型：" + type);
		}
		return downloader;
	}
	
	/**
	 * <p>新建下载任务</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public ITaskSession buildTaskSession(String url) throws DownloadException {
		final Optional<Protocol> protocol = this.protocol(url);
		if(!protocol.isPresent()) {
			throw new DownloadException("不支持的下载链接：" + url);
		}
		return protocol.get().buildTaskSession(url);
	}

	/**
	 * <p>判断是否支持下载</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return true-支持；false-不支持；
	 */
	public boolean support(String url) {
		return this.protocol(url).isPresent();
	}

	/**
	 * <p>获取下载协议</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 下载协议
	 */
	public Optional<Protocol> protocol(final String url) {
		if(StringUtils.isEmpty(url)) {
			return Optional.empty();
		}
		final String verify = url.trim();
		return this.protocols.stream()
			.filter(protocol -> protocol.available())
			.filter(protocol -> protocol.verify(verify))
			.findFirst();
	}

	/**
	 * <p>判断状态是否可用（阻塞线程）</p>
	 * 
	 * @return 是否可用
	 * 
	 * @throws DownloadException 下载异常
	 */
	public boolean available() throws DownloadException {
		if(!this.availableLock.get()) {
			synchronized (this.availableLock) {
				if(!this.availableLock.get()) {
					try {
						this.availableLock.wait(Short.MAX_VALUE);
					} catch (InterruptedException e) {
						LOGGER.debug("线程等待异常", e);
						Thread.currentThread().interrupt();
					}
				}
			}
		}
		if(Snail.available()) {
			return true;
		} else {
			throw new DownloadException("系统没有启动");
		}
	}

	/**
	 * <p>设置可用状态（释放阻塞线程）</p>
	 * 
	 * @param available 是否可用
	 */
	public void available(boolean available) {
		synchronized (this.availableLock) {
			this.availableLock.set(available);
			this.availableLock.notifyAll();
		}
	}
	
}

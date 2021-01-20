package cn.novelweb.tool.download.snail.pojo.session;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.HlsContext;
import cn.novelweb.tool.download.snail.context.SystemThreadContext;
import cn.novelweb.tool.download.snail.net.hls.HlsClient;
import cn.novelweb.tool.download.snail.pojo.IStatisticsSession;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.bean.M3u8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>HSL任务信息</p>
 * 
 * @author acgist
 */
public final class HlsSession {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HlsSession.class);

	/**
	 * <p>下载线程数量</p>
	 */
	private static final int POOL_SIZE = SystemConfig.getHlsThreadSize();
	
	/**
	 * <p>下载状态</p>
	 */
	private volatile boolean downloadable = false;
	/**
	 * <p>M3U8</p>
	 */
	private final M3u8 m3u8;
	/**
	 * <p>文件总数量</p>
	 */
	private final int fileSize;
	/**
	 * <p>累计下载大小</p>
	 */
	private final AtomicLong downloadSize;
	/**
	 * <p>任务信息</p>
	 */
	private final ITaskSession taskSession;
	/**
	 * <p>HLS下载客户端</p>
	 * <p>如果客户端下载成功移除列表，否者重新添加继续下载。</p>
	 */
	private final List<HlsClient> clients;
	/**
	 * <p>统计信息</p>
	 */
	private final IStatisticsSession statistics;
	/**
	 * <p>线程池</p>
	 */
	private ExecutorService executor;
	
	/**
	 * @param m3u8 M3U8
	 * @param taskSession 任务信息
	 */
	private HlsSession(M3u8 m3u8, ITaskSession taskSession) {
		this.m3u8 = m3u8;
		this.downloadSize = new AtomicLong();
		this.taskSession = taskSession;
		this.statistics = taskSession.statistics();
		final List<String> links = taskSession.multifileSelected();
		this.fileSize = links.size();
		this.clients = new ArrayList<>(this.fileSize);
		for (String link : links) {
			final HlsClient client = new HlsClient(link, taskSession, this);
			this.clients.add(client);
		}
	}
	
	/**
	 * <p>创建HLS任务信息</p>
	 * 
	 * @param m3u8 M3U8
	 * @param taskSession 任务信息
	 * 
	 * @return HLS任务信息
	 */
	public static final HlsSession newInstance(M3u8 m3u8, ITaskSession taskSession) {
		return new HlsSession(m3u8, taskSession);
	}
	
	/**
	 * <p>获取加密套件</p>
	 * 
	 * @return 加密套件
	 */
	public Cipher cipher() {
		if(this.m3u8 == null) {
			return null;
		}
		return this.m3u8.getCipher();
	}
	
	/**
	 * <p>开始下载</p>
	 * <p>任务全部下载结束后，如果任务可以下载并且没有完成将继续下载。</p>
	 * 
	 * @return 是否下载完成
	 */
	public boolean download() {
		if(this.downloadable) {
			LOGGER.debug("HLS任务已经开始下载");
			return false;
		}
		// 修改开始下载：提交client需要判断
		this.downloadable = true;
		this.executor = SystemThreadContext.newExecutor(POOL_SIZE, POOL_SIZE, 10000, 60L, SystemThreadContext.SNAIL_THREAD_HLS);
		synchronized (this.clients) {
			this.clients.forEach(this::download);
		}
		return this.checkCompleted();
	}
	
	/**
	 * <p>添加下载客户端</p>
	 * 
	 * @param client 客户端
	 */
	public void download(HlsClient client) {
		if(this.downloadable) {
			this.executor.submit(client);
		}
	}
	
	/**
	 * <p>移除下载客户端</p>
	 * 
	 * @param client 客户端
	 */
	public void remove(HlsClient client) {
		synchronized (this.clients) {
			this.clients.remove(client);
		}
	}
	
	/**
	 * <p>设置下载速度</p>
	 * 
	 * @param buffer 速度
	 */
	public void download(int buffer) {
		this.statistics.download(buffer);
		this.statistics.downloadLimit(buffer);
	}
	
	/**
	 * <p>设置已下载大小</p>
	 * <p>文件可能存在下载失败，这里重新计算累计下载大小。</p>
	 * 
	 * @param size 已下载大小
	 */
	public void downloadSize(long size) {
		// 设置已下载大小
		final long downloadSize = this.downloadSize.addAndGet(size);
		this.taskSession.downloadSize(downloadSize);
		// 已下载文件数量
		int downloadFileSize;
		synchronized (this.clients) {
			downloadFileSize = this.fileSize - this.clients.size();
		}
		// 预测文件总大小：存在误差
		final long taskFileSize = downloadSize * this.fileSize / downloadFileSize;
		this.taskSession.setSize(taskFileSize);
	}
	
	/**
	 * <p>判断是否可以下载</p>
	 * 
	 * @return 是否可以下载
	 */
	public boolean downloadable() {
		return this.downloadable;
	}

	/**
	 * <p>校验是否完成</p>
	 * 
	 * @return 是否完成
	 */
	public boolean checkCompleted() {
		synchronized (this.clients) {
			return this.clients.isEmpty();
		}
	}
	
	/**
	 * <p>校验是否完成</p>
	 * <p>如果完成解锁任务下载锁</p>
	 * <p>注意：需要实现幂等，文件完成会被多次调用，非幂等操作请在{@link #release()}方法中执行。</p>
	 */
	public void checkCompletedAndDone() {
		if(this.checkCompleted()) {
			this.taskSession.unlockDownload(); // 释放下载锁
		}
	}
	
	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("HLS任务释放资源：{}", this.taskSession.getName());
		this.downloadable = false;
		synchronized (this.clients) {
			this.clients.forEach(HlsClient::release);
		}
		SystemThreadContext.shutdownNow(this.executor);
	}

	/**
	 * <p>删除任务信息</p>
	 */
	public void delete() {
		HlsContext.getInstance().remove(this.taskSession);
	}
	
}

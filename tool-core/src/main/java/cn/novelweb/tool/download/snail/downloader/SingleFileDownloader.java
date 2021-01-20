package cn.novelweb.tool.download.snail.downloader;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import cn.novelweb.tool.download.snail.config.DownloadConfig;
import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.utils.IoUtils;

/**
 * <p>单文件任务下载器</p>
 * 
 * TODO：分段下载技术（断点续传支持：突破网盘限速）
 * 
 * @author acgist
 */
public abstract class SingleFileDownloader extends Downloader {
	
	/**
	 * <p>快速失败时间</p>
	 */
	private static final long FAST_CHECK_TIME = 2L * SystemConfig.ONE_SECOND_MILLIS;
	
	/**
	 * <p>输入流</p>
	 */
	protected ReadableByteChannel input;
	/**
	 * <p>输出流</p>
	 */
	protected WritableByteChannel output;
	/**
	 * <p>快速失败检测时间</p>
	 */
	private volatile long fastCheckTime;
	
	/**
	 * @param taskSession 下载任务
	 */
	protected SingleFileDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>创建{@linkplain #output 输出流}时需要验证服务端是否支持断点续传，所以优先创建{@linkplain #input 输入流}获取服务端信息。</p>
	 */
	@Override
	public void open() throws NetException, DownloadException {
		this.buildInput();
		this.buildOutput();
	}

	@Override
	public void download() throws DownloadException {
		int length = 0;
		final long fileSize = this.taskSession.getSize();
		final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH);
		try {
			while(this.downloadable()) {
				length = this.input.read(buffer);
				if(length >= 0) {
					buffer.flip();
					this.output.write(buffer);
					buffer.clear();
					this.statistics.download(length);
					this.statistics.downloadLimit(length);
					this.fastCheckTime = System.currentTimeMillis();
				}
				if(Downloader.checkFinish(length, this.taskSession.downloadSize(), fileSize)) {
					this.completed = true;
					break;
				}
			}
		} catch (Exception e) {
			throw new DownloadException("数据流操作失败", e);
		}
	}
	
	@Override
	public void unlockDownload() {
		super.unlockDownload();
		// 快速失败
		if(System.currentTimeMillis() - this.fastCheckTime > FAST_CHECK_TIME) {
			IoUtils.close(this.input);
		}
	}

	/**
	 * <p>创建{@linkplain #output 输出流}</p>
	 * <p>通过判断任务已下载大小判断是否支持断点续传</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildOutput() throws DownloadException {
		try {
			final long size = this.taskSession.downloadSize();
			final long fileSize = this.taskSession.getSize();
			final int bufferSize = DownloadConfig.getMemoryBufferByte(fileSize);
			BufferedOutputStream outputStream;
			if(size == 0L) {
				// 不支持断点续传
				outputStream = new BufferedOutputStream(new FileOutputStream(this.taskSession.getFile()), bufferSize);
			} else {
				// 支持断点续传
				outputStream = new BufferedOutputStream(new FileOutputStream(this.taskSession.getFile(), true), bufferSize);
			}
			this.output = Channels.newChannel(outputStream);
		} catch (FileNotFoundException e) {
			throw new DownloadException("下载文件打开失败", e);
		}
	}
	
	/**
	 * <p>创建{@linkplain #input 输入流}</p>
	 * <p>验证是否支持断点续传，如果支持重新设置任务已下载大小。</p>
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	protected abstract void buildInput() throws NetException, DownloadException;

}

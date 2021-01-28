package cn.novelweb.tool.download.snail.downloader.ftp;

import java.nio.channels.Channels;

import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.downloader.SingleFileDownloader;
import cn.novelweb.tool.download.snail.net.ftp.FtpClient;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.utils.FileUtils;
import cn.novelweb.tool.download.snail.utils.IoUtils;

/**
 * <p>FTP任务下载器</p>
 * 
 * @author acgist
 */
public final class FtpDownloader extends SingleFileDownloader {
	
	/**
	 * <p>FTP客户端</p>
	 */
	private FtpClient client;
	
	/**
	 * @param taskSession 任务信息
	 */
	private FtpDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>创建FTP任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link FtpDownloader}
	 */
	public static FtpDownloader newInstance(ITaskSession taskSession) {
		return new FtpDownloader(taskSession);
	}

	@Override
	public void release() {
		if(this.client != null) {
			this.client.close(); // 关闭FTP客户端
		}
		IoUtils.close(this.input);
		IoUtils.close(this.output);
		super.release();
	}
	
	@Override
	protected void buildInput() throws NetException {
		// FTP客户端
		this.client = FtpClient.newInstance(this.taskSession.getUrl());
		final boolean success = this.client.connect(); // 建立连接
		if(success) {
			// 已下载大小
			final long downloadSize = FileUtils.fileSize(this.taskSession.getFile());
			this.input = Channels.newChannel(this.client.download(downloadSize));
			if(this.client.range()) { // 支持断点续传
				this.taskSession.downloadSize(downloadSize);
			} else {
				this.taskSession.downloadSize(0L);
			}
		} else {
			this.fail("FTP服务器连接失败");
		}
	}
	
}

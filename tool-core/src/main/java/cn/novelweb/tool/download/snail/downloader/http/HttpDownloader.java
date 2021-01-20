package cn.novelweb.tool.download.snail.downloader.http;

import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.downloader.SingleFileDownloader;
import cn.novelweb.tool.download.snail.net.http.HttpClient;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.wrapper.HttpHeaderWrapper;
import cn.novelweb.tool.download.snail.utils.FileUtils;
import cn.novelweb.tool.download.snail.utils.IoUtils;

import java.nio.channels.Channels;

/**
 * <p>HTTP任务下载器</p>
 * 
 * @author acgist
 */
public final class HttpDownloader extends SingleFileDownloader {

	/**
	 * @param taskSession 任务信息
	 */
	private HttpDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>创建HTTP任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link HttpDownloader}
	 */
	public static final HttpDownloader newInstance(ITaskSession taskSession) {
		return new HttpDownloader(taskSession);
	}
	
	@Override
	public void release() {
		IoUtils.close(this.input);
		IoUtils.close(this.output);
		super.release();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see HttpHeaderWrapper#HEADER_RANGE
	 */
	@Override
	protected void buildInput() throws NetException {
		// 已下载大小
		final long downloadSize = FileUtils.fileSize(this.taskSession.getFile());
		// HTTP客户端
		final HttpClient client = HttpClient
			.newDownloader(this.taskSession.getUrl())
			.range(downloadSize)
			.get();
		// 请求成功和部分请求成功
		if(client.downloadable()) {
			final HttpHeaderWrapper headers = client.responseHeader();
			this.input = Channels.newChannel(client.response());
			if(headers.range()) { // 支持断点续传
				headers.verifyBeginRange(downloadSize);
				this.taskSession.downloadSize(downloadSize);
			} else {
				this.taskSession.downloadSize(0L);
			}
		} else if(client.requestedRangeNotSatisfiable()) {
			if(this.taskSession.downloadSize() == this.taskSession.getSize()) {
				this.completed = true;
			} else {
				this.fail("无法满足文件下载范围：" + downloadSize);
			}
		} else {
			this.fail("HTTP请求失败：" + client.code());
		}
	}

}

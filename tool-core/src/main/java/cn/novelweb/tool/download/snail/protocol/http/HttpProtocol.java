package cn.novelweb.tool.download.snail.protocol.http;

import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.downloader.IDownloader;
import cn.novelweb.tool.download.snail.downloader.http.HttpDownloader;
import cn.novelweb.tool.download.snail.net.http.HttpClient;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.wrapper.HttpHeaderWrapper;
import cn.novelweb.tool.download.snail.protocol.Protocol;

/**
 * <p>HTTP协议</p>
 * 
 * @author acgist
 */
public final class HttpProtocol extends Protocol {

	private static final HttpProtocol INSTANCE = new HttpProtocol();
	
	public static final HttpProtocol getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>HTTP头部信息</p>
	 */
	private HttpHeaderWrapper httpHeaderWrapper;
	
	private HttpProtocol() {
		super(Type.HTTP);
	}
	
	@Override
	public String name() {
		return "HTTP";
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	public IDownloader buildDownloader(ITaskSession taskSession) {
		return HttpDownloader.newInstance(taskSession);
	}

	@Override
	protected void prep() throws DownloadException {
		this.buildHttpHeader();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>优先使用头部信息中的文件名称</p>
	 */
	@Override
	protected String buildFileName() throws DownloadException {
		final String defaultName = super.buildFileName();
		return this.httpHeaderWrapper.fileName(defaultName);
	}

	@Override
	protected void buildSize() {
		this.taskEntity.setSize(this.httpHeaderWrapper.fileSize());
	}
	
	@Override
	protected void release(boolean success) {
		super.release(success);
		this.httpHeaderWrapper = null;
	}

	/**
	 * <p>获取HTTP头部信息</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void buildHttpHeader() throws DownloadException {
		try {
			this.httpHeaderWrapper = HttpClient
				.newInstance(this.url)
				.head()
				.responseHeader();
		} catch (NetException e) {
			throw new DownloadException("获取HTTP头部信息失败", e);
		}
	}
	
}

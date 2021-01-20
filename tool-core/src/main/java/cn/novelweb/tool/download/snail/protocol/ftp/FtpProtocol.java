package cn.novelweb.tool.download.snail.protocol.ftp;

import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.downloader.IDownloader;
import cn.novelweb.tool.download.snail.downloader.ftp.FtpDownloader;
import cn.novelweb.tool.download.snail.net.ftp.FtpClient;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.protocol.Protocol;

/**
 * <p>FTP协议</p>
 * 
 * @author acgist
 */
public final class FtpProtocol extends Protocol {
	
	private static final FtpProtocol INSTANCE = new FtpProtocol();
	
	public static final FtpProtocol getInstance() {
		return INSTANCE;
	}
	
	private FtpProtocol() {
		super(Type.FTP);
	}

	@Override
	public String name() {
		return "FTP";
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	public IDownloader buildDownloader(ITaskSession taskSession) {
		return FtpDownloader.newInstance(taskSession);
	}

	@Override
	protected void buildSize() throws DownloadException {
		final FtpClient client = FtpClient.newInstance(this.url);
		try {
			client.connect();
			final long size = client.size();
			this.taskEntity.setSize(size);
		} catch (NetException e) {
			throw new DownloadException(e);
		} finally {
			client.close();
		}
	}

}

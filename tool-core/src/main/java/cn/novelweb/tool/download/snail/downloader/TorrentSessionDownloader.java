package cn.novelweb.tool.download.snail.downloader;

import cn.novelweb.tool.download.snail.context.TorrentContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.bean.Magnet;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.protocol.magnet.MagnetBuilder;

/**
 * <p>BT任务下载器</p>
 * 
 * @author acgist
 */
public abstract class TorrentSessionDownloader extends MultifileDownloader {
	
	/**
	 * <p>BT任务信息</p>
	 */
	protected TorrentSession torrentSession;
	
	/**
	 * @param taskSession 任务信息
	 */
	protected TorrentSessionDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	@Override
	public void open() throws NetException, DownloadException {
		// 不能在构造函数中初始化：防止种子被删除后还能点击下载
		this.torrentSession = this.loadTorrentSession();
		super.open();
	}
	
	/**
	 * <p>加载BT任务信息</p>
	 * 
	 * @return BT任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected TorrentSession loadTorrentSession() throws DownloadException {
		final String torrentPath = this.taskSession.getTorrent();
		// 加载磁力链接信息
		final Magnet magnet = MagnetBuilder.newInstance(this.taskSession.getUrl()).build();
		final String infoHashHex = magnet.getHash();
		return TorrentContext.getInstance().newTorrentSession(infoHashHex, torrentPath);
	}
	
	@Override
	protected boolean checkCompleted() {
		return this.torrentSession.checkCompleted();
	}

}

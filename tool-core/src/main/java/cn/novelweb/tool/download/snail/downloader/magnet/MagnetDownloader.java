package cn.novelweb.tool.download.snail.downloader.magnet;

import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.downloader.TorrentSessionDownloader;
import cn.novelweb.tool.download.snail.downloader.torrent.TorrentDownloader;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;

/**
 * <p>磁力链接任务下载器</p>
 * <p>下载原理：先将磁力链接转为种子文件，然后转为{@link TorrentDownloader}进行下载。</p>
 * 
 * @author acgist
 */
public final class MagnetDownloader extends TorrentSessionDownloader {
	
	/**
	 * @param taskSession 任务信息
	 */
	private MagnetDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	/**
	 * <p>创建磁力链接任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link MagnetDownloader}
	 */
	public static final MagnetDownloader newInstance(ITaskSession taskSession) {
		return new MagnetDownloader(taskSession);
	}

	@Override
	public void release() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseMagnet(); // 释放磁力链接资源
			// 任务没有删除：留着任务信息转为BT任务继续使用
			if(this.statusDelete()) {
				this.delete();
			}
		}
		super.release();
	}
	
	@Override
	public void delete() {
		super.delete();
		if(this.torrentSession != null) {
			this.torrentSession.delete(); // 删除任务信息
		}
	}
	
	@Override
	protected void loadDownload() throws DownloadException {
		this.completed = this.torrentSession.magnet(this.taskSession);
	}

}

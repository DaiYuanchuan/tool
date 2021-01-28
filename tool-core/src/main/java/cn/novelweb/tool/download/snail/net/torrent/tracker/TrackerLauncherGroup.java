package cn.novelweb.tool.download.snail.net.torrent.tracker;

import cn.novelweb.tool.download.snail.config.PeerConfig;
import cn.novelweb.tool.download.snail.config.PeerConfig.Action;
import cn.novelweb.tool.download.snail.context.SystemThreadContext;
import cn.novelweb.tool.download.snail.context.TrackerContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.pojo.bean.Magnet;
import cn.novelweb.tool.download.snail.pojo.bean.Torrent;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.pojo.session.TrackerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Tracker执行器组</p>
 * <p>Tracker执行器加载和管理</p>
 * 
 * @author acgist
 */
public final class TrackerLauncherGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncherGroup.class);
	
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	/**
	 * <p>Tracker执行器集合</p>
	 */
	private final List<TrackerLauncher> trackerLaunchers;
	
	/**
	 * @param torrentSession BT任务信息
	 */
	private TrackerLauncherGroup(TorrentSession torrentSession) {
		this.torrentSession = torrentSession;
		this.trackerLaunchers = new ArrayList<>();
	}
	
	/**
	 * <p>创建Tracker执行器组</p>
	 * 
	 * @param torrentSession BT任务信息
	 * 
	 * @return Tracker执行器组
	 */
	public static TrackerLauncherGroup newInstance(TorrentSession torrentSession) {
		return new TrackerLauncherGroup(torrentSession);
	}

	/**
	 * <p>获取当前使用的所有Tracker服务器声明地址</p>
	 * 
	 * @return 当前使用的所有Tracker服务器声明地址
	 */
	public List<String> trackers() {
		synchronized (this.trackerLaunchers) {
			return this.trackerLaunchers.stream()
				.map(TrackerLauncher::announceUrl)
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * <p>加载TrackerLauncher</p>
	 * <p>优先使用种子的Tracker，如果数量不够可以从系统Tracker列表中添加。</p>
	 * <p>私有种子不从系统Tracker列表中添加</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	public void loadTracker() throws DownloadException {
		List<TrackerSession> sessions = null;
		final PeerConfig.Action action = this.torrentSession.action(); // 下载动作
		if(action == Action.TORRENT) { // BT任务
			final Torrent torrent = this.torrentSession.torrent();
			sessions = TrackerContext.getInstance().sessions(torrent.getAnnounce(), torrent.getAnnounceList(), this.torrentSession.isPrivateTorrent());
		} else if(action == Action.MAGNET) { // 磁力链接任务
			final Magnet magnet = this.torrentSession.magnet();
			sessions = TrackerContext.getInstance().sessions(magnet.getTr());
		} else {
			LOGGER.warn("加载TrackerLauncher失败（未知动作）：{}", action);
			return;
		}
		final List<TrackerLauncher> list = sessions.stream()
			.map(client -> TrackerContext.getInstance().buildTrackerLauncher(client, this.torrentSession))
			.collect(Collectors.toList());
		synchronized (this.trackerLaunchers) {
			this.trackerLaunchers.addAll(list);
		}
	}

	/**
	 * <p>查找Peer</p>
	 * 
	 * @see TrackerLauncher#findPeer()
	 */
	public void findPeer() {
		final List<TrackerLauncher> list;
		// 新建集合进行查找：防止释放资源等待
		synchronized (this.trackerLaunchers) {
			list = new ArrayList<>(this.trackerLaunchers);
		}
		list.forEach(TrackerLauncher::findPeer);
	}

	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("释放TrackerLauncherGroup");
		synchronized (this.trackerLaunchers) {
			this.trackerLaunchers.forEach(launcher -> SystemThreadContext.submit(launcher::release));
			this.trackerLaunchers.clear();
		}
	}
	
}

package cn.novelweb.tool.download.snail.pojo.session;

import cn.novelweb.tool.download.snail.config.TrackerConfig;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.net.torrent.tracker.TrackerLauncher;
import cn.novelweb.tool.download.snail.pojo.IStatisticsSession;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.protocol.Protocol;
import cn.novelweb.tool.download.snail.utils.BeanUtils;
import cn.novelweb.tool.download.snail.utils.NumberUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * <p>Tracker信息</p>
 * <p>基本协议：TCP（HTTP）、UDP</p>
 * <p>sid：{@link TrackerLauncher#id()}</p>
 * 
 * @author acgist
 */
public abstract class TrackerSession implements Comparable<TrackerSession> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerSession.class);
	
	/**
	 * <p>想要获取的Peer数量：{@value}</p>
	 */
	protected static final int WANT_PEER_SIZE = 50;
	
	/**
	 * <p>权重</p>
	 * <p>查询成功会使权重增加、查询失败会使权重减少</p>
	 */
	protected int weight;
	/**
	 * <p>客户端ID</p>
	 * <p>ID与Tracker服务器一一对应</p>
	 */
	protected final Integer id;
	/**
	 * <p>协议类型</p>
	 */
	protected final Protocol.Type type;
	/**
	 * <p>刮檫地址</p>
	 */
	protected final String scrapeUrl;
	/**
	 * <p>声明地址</p>
	 */
	protected final String announceUrl;
	/**
	 * <p>失败次数</p>
	 * <p>请求处理成功后清零，超过{@linkplain TrackerConfig#MAX_FAIL_TIMES 最大失败次数}设置为不可用。</p>
	 */
	private int failTimes = 0;
	/**
	 * <p>是否可用</p>
	 */
	private boolean available = true;
	/**
	 * <p>失败原因</p>
	 */
	private String failMessage;
	
	/**
	 * <p>Tracker客户端</p>
	 * 
	 * @param scrapeUrl 刮擦URL
	 * @param announceUrl 声明URL
	 * @param type 协议类型
	 * 
	 * @throws NetException 网络异常
	 */
	protected TrackerSession(String scrapeUrl, String announceUrl, Protocol.Type type) throws NetException {
		if(StringUtils.isEmpty(announceUrl)) {
			throw new NetException("Tracker声明地址错误：" + announceUrl);
		}
		this.id = NumberUtils.build();
		this.type = type;
		this.weight = 0;
		this.scrapeUrl = scrapeUrl;
		this.announceUrl = announceUrl;
	}

	/**
	 * <p>查找Peer</p>
	 * <p>查找到的结果放入Peer列表</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 */
	public void findPeers(Integer sid, TorrentSession torrentSession) {
		if(!this.available()) {
			return;
		}
		try {
			this.started(sid, torrentSession); // 发送声明消息
			this.weight++;
			this.failTimes = 0; // 成功直接清空失败次数
		} catch (Exception e) {
			LOGGER.error("查找Peer异常，失败次数：{}，声明地址：{}", this.failTimes, this.announceUrl, e);
			this.weight--;
			if(++this.failTimes >= TrackerConfig.MAX_FAIL_TIMES) {
				LOGGER.warn("TrackerSession停用，失败次数：{}，声明地址：{}", this.failTimes, this.announceUrl);
				this.available = false;
				this.failMessage = e.getMessage();
			}
		}
	}
	
	/**
	 * <p>声明：开始</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void started(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>声明：完成</p>
	 * <p>任务完成时发送</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void completed(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>声明：停止</p>
	 * <p>任务暂停时发送</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void stopped(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>刮檫</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void scrape(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>创建声明消息</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * @param event 事件
	 * 
	 * @return 声明消息
	 */
	protected Object buildAnnounceMessage(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event) {
		long download = 0L, left = 0L, upload = 0L;
		final ITaskSession taskSession = torrentSession.taskSession();
		if(taskSession != null) {
			final IStatisticsSession statistics = taskSession.statistics();
			download = statistics.downloadSize(); // 已下载
			left = taskSession.getSize() - download; // 剩余下载
			upload = statistics.uploadSize(); // 已上传
		}
		return this.buildAnnounceMessageEx(sid, torrentSession, event, download, left, upload);
	}
	
	/**
	 * <p>创建声明消息</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * @param event 事件
	 * @param download 已下载大小
	 * @param left 剩余大小
	 * @param upload 已上传大小
	 * 
	 * @return 声明消息
	 */
	protected abstract Object buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long left, long upload);
	
	/**
	 * <p>获取客户端ID</p>
	 * 
	 * @return 客户端ID
	 */
	public Integer id() {
		return this.id;
	}
	
	/**
	 * <p>获取客户端类型</p>
	 * 
	 * @return 客户端类型
	 */
	public Protocol.Type type() {
		return this.type;
	}
	
	/**
	 * <p>获取刮擦URL</p>
	 * 
	 * @return 刮擦URL
	 */
	public String scrapeUrl() {
		return this.scrapeUrl;
	}
	
	/**
	 * <p>获取客户端声明URL</p>
	 * 
	 * @return 客户端声明URL
	 */
	public String announceUrl() {
		return this.announceUrl;
	}
	
	/**
	 * <p>判断是否可用</p>
	 * 
	 * @return true-可用；false-禁用；
	 */
	public boolean available() {
		return this.available;
	}
	
	/**
	 * <p>获取失败原因</p>
	 * 
	 * @return 失败原因
	 */
	public String failMessage() {
		return this.failMessage;
	}
	
	/**
	 * <p>判断当前客户端的声明URL和声明URL是否相同</p>
	 * 
	 * @param announceUrl 声明地址
	 * 
	 * @return true-相同；fasle-不同；
	 */
	public boolean equalsAnnounceUrl(String announceUrl) {
		return this.announceUrl.equals(announceUrl);
	}
	
	@Override
	public int compareTo(TrackerSession session) {
		return Integer.compare(this.weight, session.weight);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.announceUrl);
	}
	
	/**
	 * <p>客户端声明地址相同时相等</p>
	 * 
	 * @param object 客户端
	 * 
	 * @return true-相等；false-不等；
	 */
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof TrackerSession) {
			final TrackerSession session = (TrackerSession) object;
			return StringUtils.equals(this.announceUrl, session.announceUrl);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.id, this.type, this.failTimes, this.announceUrl);
	}
	
}

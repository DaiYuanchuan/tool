package cn.novelweb.tool.download.snail.net.torrent.tracker;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.config.TrackerConfig;
import cn.novelweb.tool.download.snail.context.TrackerContext;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.format.BEncodeDecoder;
import cn.novelweb.tool.download.snail.net.http.HttpClient;
import cn.novelweb.tool.download.snail.net.torrent.peer.PeerService;
import cn.novelweb.tool.download.snail.pojo.message.AnnounceMessage;
import cn.novelweb.tool.download.snail.pojo.message.ScrapeMessage;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.pojo.session.TrackerSession;
import cn.novelweb.tool.download.snail.protocol.Protocol;
import cn.novelweb.tool.download.snail.utils.PeerUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>HTTP Tracker信息</p>
 * <p>协议链接：https://wiki.theory.org/index.php/BitTorrentSpecification</p>
 * <p>The BitTorrent Protocol Specification</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0003.html</p>
 * <p>Tracker Returns Compact Peer Lists</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0023.html</p>
 * <p>Tracker Protocol Extension: Scrape</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0048.html</p>
 *
 * @author acgist
 */
public final class HttpTrackerSession extends TrackerSession {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTrackerSession.class);

	/**
	 * <p>刮檫URL：{@value}</p>
	 */
	private static final String SCRAPE_URL_SUFFIX = "/scrape";
	/**
	 * <p>声明URL：{@value}</p>
	 */
	private static final String ANNOUNCE_URL_SUFFIX = "/announce";

	/**
	 * <p>跟踪器ID</p>
	 * <p>第一次收到响应时获取，以后每次发送声明消息时上送。</p>
	 */
	private String trackerId;

	/**
	 * @param scrapeUrl 刮擦URL
	 * @param announceUrl 声明URL
	 *
	 * @throws NetException 网络异常
	 */
	private HttpTrackerSession(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.Type.HTTP);
	}

	/**
	 * <p>创建HttpTrackerSession</p>
	 *
	 * @param announceUrl 声明地址
	 *
	 * @return HttpTrackerSession
	 *
	 * @throws NetException 网络异常
	 */
	public static HttpTrackerSession newInstance(String announceUrl) throws NetException {
		final String scrapeUrl = buildScrapeUrl(announceUrl);
		return new HttpTrackerSession(scrapeUrl, announceUrl);
	}

	@Override
	public void started(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STARTED);
		final HttpClient client = HttpClient
			.newInstance(announceMessage)
			.get();
		if(!client.ok()) {
			throw new NetException("HTTP Tracker声明失败");
		}
		final byte[] body = client.responseToBytes();
		final BEncodeDecoder decoder = BEncodeDecoder.newInstance(body);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			throw new NetException("HTTP Tracker声明消息错误（格式）：" + decoder.oddString());
		}
		final AnnounceMessage message = convertAnnounceMessage(sid, decoder);
		this.trackerId = message.getTrackerId(); // 跟踪器ID
		TrackerContext.getInstance().announce(message);
	}

	@Override
	public void completed(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.COMPLETED);
		HttpClient.newInstance(announceMessage).get();
	}

	@Override
	public void stopped(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STOPPED);
		HttpClient.newInstance(announceMessage).get();
	}

	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		final String scrapeMessage = this.buildScrapeMessage(sid, torrentSession);
		if(scrapeMessage == null) {
			LOGGER.debug("HTTP Tracker刮檫消息错误：{}", this.announceUrl);
			return;
		}
		final HttpClient client = HttpClient
			.newInstance(scrapeMessage)
			.get();
		if(!client.ok()) {
			throw new NetException("HTTP Tracker刮檫失败");
		}
		final byte[] body = client.responseToBytes();
		final BEncodeDecoder decoder = BEncodeDecoder.newInstance(body);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("HTTP Tracker刮檫消息错误（格式）：{}", decoder.oddString());
			return;
		}
		final List<ScrapeMessage> messages = convertScrapeMessage(sid, decoder);
		messages.forEach(message -> TrackerContext.getInstance().scrape(message));
	}

	@Override
	protected String buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long left, long upload) {
		final StringBuilder builder = new StringBuilder(this.announceUrl);
		builder.append("?")
			.append("info_hash").append("=").append(torrentSession.infoHash().infoHashUrl()).append("&") // InfoHash
			.append("peer_id").append("=").append(PeerService.getInstance().peerIdUrl()).append("&") // PeerId
			.append("port").append("=").append(SystemConfig.getTorrentPortExtShort()).append("&") // 外网Peer端口
			.append("uploaded").append("=").append(upload).append("&") // 已上传大小
			.append("downloaded").append("=").append(download).append("&") // 已下载大小
			.append("left").append("=").append(left).append("&") // 剩余下载大小
			.append("compact").append("=").append("1").append("&") // 默认：1（紧凑）
			.append("event").append("=").append(event.value()).append("&") // 事件：completed、started、stopped
			.append("numwant").append("=").append(WANT_PEER_SIZE); // 想要获取的Peer数量
		if(StringUtils.isNotEmpty(this.trackerId)) {
			builder.append("&").append("trackerid").append("=").append(this.trackerId); // 跟踪器ID
		}
		return builder.toString();
	}

	/**
	 * <p>创建刮檫消息</p>
	 *
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 *
	 * @return 刮檫消息
	 */
	private String buildScrapeMessage(Integer sid, TorrentSession torrentSession) {
		if(StringUtils.isEmpty(this.scrapeUrl)) {
			return null;
		}
		final StringBuilder builder = new StringBuilder(this.scrapeUrl);
		builder.append("?")
			.append("info_hash").append("=").append(torrentSession.infoHash().infoHashUrl());
		return builder.toString();
	}

	/**
	 * <p>声明消息转换</p>
	 *
	 * @param sid sid
	 * @param decoder B编码解码器
	 *
	 * @return 声明消息
	 */
	private static final AnnounceMessage convertAnnounceMessage(Integer sid, BEncodeDecoder decoder) {
		final String trackerId = decoder.getString("tracker id");
		final Integer complete = decoder.getInteger("complete");
		final Integer incomplete = decoder.getInteger("incomplete");
		final Integer interval = decoder.getInteger("interval");
		final Integer minInterval = decoder.getInteger("min interval");
		final String warngingMessage = decoder.getString("warnging message");
		final String failureReason = decoder.getString("failure reason");
		final Object peersObject = decoder.get("peers");
		Map<String, Integer> peers;
		if(peersObject instanceof byte[]) {
			peers = PeerUtils.read((byte[]) peersObject);
		} else {
			peers = new HashMap<>();
			// TODO：List解析
			LOGGER.warn("Peer声明消息格式没有适配：{}", peersObject);
		}
		final AnnounceMessage message = new AnnounceMessage();
		message.setId(sid);
		if(StringUtils.isNotEmpty(failureReason)) {
			LOGGER.warn("HTTP Tracker声明失败：{}", failureReason);
			return message;
		}
		if(StringUtils.isNotEmpty(warngingMessage)) {
			LOGGER.warn("HTTP Tracker声明警告：{}", failureReason);
		}
		message.setTrackerId(trackerId);
		if(interval != null && minInterval != null) {
			message.setInterval(Math.min(interval, minInterval));
		} else {
			message.setInterval(interval);
		}
		message.setLeecher(incomplete);
		message.setSeeder(complete);
		message.setPeers(peers);
		return message;
	}

	/**
	 * <p>刮檫消息转换</p>
	 *
	 * @param sid sid
	 * @param decoder B编码解码器
	 *
	 * @return 刮擦消息
	 */
	private static final List<ScrapeMessage> convertScrapeMessage(Integer sid, BEncodeDecoder decoder) {
		final Map<String, Object> files = decoder.getMap("files");
		if(files == null) {
			LOGGER.debug("HTTP Tracker刮檫消息错误：{}", decoder.oddString());
			return Collections.emptyList();
		}
		return files.values().stream()
			.filter(Objects::nonNull)
			.map(value -> {
				final Map<?, ?> map = (Map<?, ?>) value;
				final ScrapeMessage message = new ScrapeMessage();
				message.setId(sid);
				message.setSeeder(BEncodeDecoder.getInteger(map, "downloaded"));
				message.setCompleted(BEncodeDecoder.getInteger(map, "complete"));
				message.setLeecher(BEncodeDecoder.getInteger(map, "incomplete"));
				return message;
			})
			.collect(Collectors.toList());
	}

	/**
	 * <p>announceUrl转换scrapeUrl</p>
	 * <table border="1">
	 * 	<tr>
	 * 		<td>~http://example.com/announce</td>
	 * 		<td>~http://example.com/scrape</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/x/announce</td>
	 * 		<td>~http://example.com/x/scrape</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/announce.php</td>
	 * 		<td>~http://example.com/scrape.php</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/a</td>
	 * 		<td>(scrape not supported)</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/announce?x2%0644</td>
	 * 		<td>~http://example.com/scrape?x2%0644</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/announce?x=2/4</td>
	 * 		<td>(scrape not supported)</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/x%064announce</td>
	 * 		<td>(scrape not supported)</td>
	 * 	</tr>
	 * </table>
	 *
	 * @param url 声明URL
	 *
	 * @return 刮檫URL
	 */
	private static final String buildScrapeUrl(String url) {
		if(url != null && url.contains(ANNOUNCE_URL_SUFFIX)) {
			return url.replace(ANNOUNCE_URL_SUFFIX, SCRAPE_URL_SUFFIX);
		}
		return null;
	}

}

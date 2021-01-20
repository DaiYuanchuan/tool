package cn.novelweb.tool.download.snail.net.torrent.dht;

import cn.novelweb.tool.download.snail.context.NodeContext;
import cn.novelweb.tool.download.snail.pojo.bean.InfoHash;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.utils.CollectionUtils;
import cn.novelweb.tool.download.snail.utils.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>DHT定时任务</p>
 * <p>定时使用系统最近的DHT节点和{@link #peerNodes}查询Peer</p>
 * 
 * @author acgist
 */
public final class DhtLauncher implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtLauncher.class);
	
	/**
	 * <p>种子信息</p>
	 */
	private final InfoHash infoHash;
	/**
	 * <p>客户端节点队列</p>
	 * <p>如果连接的Peer支持DHT，将该Peer作为节点在下次查询时使用并加入到系统节点。</p>
	 */
	private final List<InetSocketAddress> peerNodes = new ArrayList<>();
	
	/**
	 * @param torrentSession BT任务信息
	 */
	private DhtLauncher(TorrentSession torrentSession) {
		this.infoHash = torrentSession.infoHash();
	}
	
	/**
	 * <p>创建DHT定时任务</p>
	 * 
	 * @param torrentSession BT任务信息
	 * 
	 * @return DhtLauncher
	 */
	public static final DhtLauncher newInstance(TorrentSession torrentSession) {
		return new DhtLauncher(torrentSession);
	}
	
	@Override
	public void run() {
		LOGGER.debug("执行DHT定时任务");
		List<InetSocketAddress> nodes;
		synchronized (this.peerNodes) {
			nodes = new ArrayList<>(this.peerNodes);
			this.peerNodes.clear(); // 清空节点信息
		}
		try {
			final List<InetSocketAddress> list = this.pick();
			if(CollectionUtils.isNotEmpty(nodes)) {
				this.joinNodes(nodes);
				list.addAll(nodes);
			}
			this.findPeers(list);
		} catch (Exception e) {
			LOGGER.error("执行DHT定时任务异常", e);
		}
	}
	
	/**
	 * <p>添加DHT Peer客户端</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @see #peerNodes
	 */
	public void put(String host, Integer port) {
		synchronized (this.peerNodes) {
			this.peerNodes.add(NetUtils.buildSocketAddress(host, port));
		}
	}
	
	/**
	 * <p>挑选DHT节点</p>
	 * 
	 * @return DHT节点
	 */
	private List<InetSocketAddress> pick() {
		return NodeContext.getInstance().findNode(this.infoHash.infoHash()).stream()
			.map(node -> NetUtils.buildSocketAddress(node.getHost(), node.getPort()))
			.collect(Collectors.toList());
	}

	/**
	 * <p>将DHT Peer客户端加入系统节点</p>
	 * 
	 * @param peerNodes DHT Peer客户端
	 * 
	 * @see #peerNodes
	 */
	private void joinNodes(List<InetSocketAddress> peerNodes) {
		final NodeContext nodeContext = NodeContext.getInstance();
		peerNodes.forEach(address -> nodeContext.newNodeSession(address.getHostString(), address.getPort()));
	}
	
	/**
	 * <p>使用DHT节点查询Peer</p>
	 * 
	 * @param list DHT节点
	 */
	private void findPeers(List<InetSocketAddress> list) {
		if(CollectionUtils.isEmpty(list)) {
			LOGGER.debug("DHT定时任务没有节点可用");
			return;
		}
		final byte[] infoHashValue = this.infoHash.infoHash();
		for (InetSocketAddress socketAddress : list) {
			DhtClient.newInstance(socketAddress).getPeers(infoHashValue);
		}
	}
	
}

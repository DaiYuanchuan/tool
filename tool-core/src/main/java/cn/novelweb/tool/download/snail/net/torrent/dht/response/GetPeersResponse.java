package cn.novelweb.tool.download.snail.net.torrent.dht.response;

import cn.novelweb.tool.download.snail.config.DhtConfig;
import cn.novelweb.tool.download.snail.config.PeerConfig;
import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.DhtContext;
import cn.novelweb.tool.download.snail.context.PeerContext;
import cn.novelweb.tool.download.snail.context.TorrentContext;
import cn.novelweb.tool.download.snail.net.torrent.dht.DhtRequest;
import cn.novelweb.tool.download.snail.net.torrent.dht.DhtResponse;
import cn.novelweb.tool.download.snail.pojo.session.NodeSession;
import cn.novelweb.tool.download.snail.pojo.session.PeerSession;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.utils.NetUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>查找Peer</p>
 * 
 * @author acgist
 */
public final class GetPeersResponse extends DhtResponse {

	/**
	 * @param t 节点ID
	 */
	private GetPeersResponse(byte[] t) {
		super(t);
		// 设置Token：声明Peer时使用
		this.put(DhtConfig.KEY_TOKEN, DhtContext.getInstance().token());
	}
	
	/**
	 * @param response 响应
	 */
	private GetPeersResponse(DhtResponse response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}
	
	/**
	 * <p>创建响应</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	public static GetPeersResponse newInstance(DhtRequest request) {
		return new GetPeersResponse(request.getT());
	}

	/**
	 * <p>创建响应</p>
	 * 
	 * @param response 响应
	 * 
	 * @return 响应
	 */
	public static GetPeersResponse newInstance(DhtResponse response) {
		return new GetPeersResponse(response);
	}
	
	/**
	 * <p>获取Token</p>
	 * 
	 * @return Token
	 */
	public byte[] getToken() {
		return this.getBytes(DhtConfig.KEY_TOKEN);
	}
	
	/**
	 * <p>获取节点列表</p>
	 * <p>同时加入系统</p>
	 * 
	 * @return 节点列表
	 */
	public List<NodeSession> getNodes() {
		final byte[] bytes = this.getBytes(DhtConfig.KEY_NODES);
		return deserializeNodes(bytes);
	}

	/**
	 * <p>获取Peer列表</p>
	 * 
	 * @param infoHashHex InfoHash Hex
	 * 
	 * @return Peer列表
	 * 
	 * @see #getValues(String)
	 */
	public List<PeerSession> getPeers(String infoHashHex) {
		return this.getValues(infoHashHex);
	}
	
	/**
	 * <p>获取Peer列表</p>
	 * <p>同时加入系统</p>
	 * 
	 * @param infoHashHex InfoHash Hex
	 * 
	 * @return Peer列表
	 */
	public List<PeerSession> getValues(String infoHashHex) {
		final TorrentSession torrentSession = TorrentContext.getInstance().torrentSession(infoHashHex);
		if(torrentSession == null) {
			return Collections.emptyList();
		}
		final List<?> values = this.getList(DhtConfig.KEY_VALUES);
		if(values == null) {
			return Collections.emptyList();
		}
		PeerSession session;
		final ByteBuffer buffer = ByteBuffer.allocate(SystemConfig.IP_PORT_LENGTH);
		final List<PeerSession> list = new ArrayList<>();
		for (Object object : values) {
			buffer.put((byte[]) object);
			buffer.flip();
			session = PeerContext.getInstance().newPeerSession(
				infoHashHex,
				torrentSession.statistics(),
				NetUtils.intToIP(buffer.getInt()),
				NetUtils.portToInt(buffer.getShort()),
				PeerConfig.Source.DHT
			);
			buffer.flip();
			list.add(session);
		}
		return list;
	}
	
	/**
	 * <p>判断是否含有节点</p>
	 * 
	 * @return 是否含有节点
	 */
	public boolean hasNodes() {
		return this.get(DhtConfig.KEY_NODES) != null;
	}
	
	/**
	 * <p>判断是否含有Peer</p>
	 * 
	 * @return 是否含有Peer
	 * 
	 * @see #hasValues()
	 */
	public boolean hasPeers() {
		return this.hasValues();
	}
	
	/**
	 * <p>判断是否含有Peer</p>
	 * 
	 * @return 是否含有Peer
	 */
	public boolean hasValues() {
		return this.get(DhtConfig.KEY_VALUES) != null;
	}

}

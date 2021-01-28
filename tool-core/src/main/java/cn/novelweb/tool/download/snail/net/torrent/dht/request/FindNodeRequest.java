package cn.novelweb.tool.download.snail.net.torrent.dht.request;

import cn.novelweb.tool.download.snail.config.DhtConfig;
import cn.novelweb.tool.download.snail.context.NodeContext;
import cn.novelweb.tool.download.snail.net.torrent.dht.DhtRequest;
import cn.novelweb.tool.download.snail.net.torrent.dht.response.FindNodeResponse;
import cn.novelweb.tool.download.snail.pojo.session.NodeSession;

import java.util.List;

/**
 * <p>查找Node</p>
 * 
 * @author acgist
 */
public final class FindNodeRequest extends DhtRequest {

	private FindNodeRequest() {
		super(DhtConfig.QType.FIND_NODE);
	}
	
	/**
	 * <p>创建请求</p>
	 * 
	 * @param target NodeId或者InfoHash
	 * 
	 * @return 请求
	 */
	public static FindNodeRequest newRequest(byte[] target) {
		final FindNodeRequest request = new FindNodeRequest();
		request.put(DhtConfig.KEY_TARGET, target);
		return request;
	}

	/**
	 * <p>处理请求</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	public static FindNodeResponse execute(DhtRequest request) {
		final FindNodeResponse response = FindNodeResponse.newInstance(request);
		final byte[] target = request.getBytes(DhtConfig.KEY_TARGET);
		final List<NodeSession> nodes = NodeContext.getInstance().findNode(target);
		response.put(DhtConfig.KEY_NODES, serializeNodes(nodes));
		return response;
	}
	
	/**
	 * <p>获取NodeId或者InfoHash</p>
	 * 
	 * @return NodeId或者InfoHash
	 */
	public byte[] getTarget() {
		return this.getBytes(DhtConfig.KEY_TARGET);
	}

}

package cn.novelweb.tool.download.snail.net.torrent.dht.request;

import cn.novelweb.tool.download.snail.config.DhtConfig;
import cn.novelweb.tool.download.snail.net.torrent.dht.DhtRequest;
import cn.novelweb.tool.download.snail.net.torrent.dht.response.PingResponse;

/**
 * <p>Ping</p>
 * 
 * @author acgist
 */
public final class PingRequest extends DhtRequest {

	private PingRequest() {
		super(DhtConfig.QType.PING);
	}
	
	/**
	 * <p>创建请求</p>
	 * 
	 * @return 请求
	 */
	public static final PingRequest newRequest() {
		return new PingRequest();
	}

	/**
	 * <p>处理请求</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	public static final PingResponse execute(DhtRequest request) {
		return PingResponse.newInstance(request);
	}
	
}

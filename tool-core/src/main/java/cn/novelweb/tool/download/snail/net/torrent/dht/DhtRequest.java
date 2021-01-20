package cn.novelweb.tool.download.snail.net.torrent.dht;

import cn.novelweb.tool.download.snail.config.DhtConfig;
import cn.novelweb.tool.download.snail.config.DhtConfig.QType;
import cn.novelweb.tool.download.snail.context.DhtContext;
import cn.novelweb.tool.download.snail.context.NodeContext;
import cn.novelweb.tool.download.snail.format.BEncodeDecoder;
import cn.novelweb.tool.download.snail.format.BEncodeEncoder;
import cn.novelweb.tool.download.snail.pojo.session.NodeSession;
import cn.novelweb.tool.download.snail.utils.ArrayUtils;
import cn.novelweb.tool.download.snail.utils.BeanUtils;
import cn.novelweb.tool.download.snail.utils.CollectionUtils;
import cn.novelweb.tool.download.snail.utils.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>DHT请求</p>
 * 
 * @author acgist
 */
public class DhtRequest extends DhtMessage {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtRequest.class);

	/**
	 * <p>请求类型</p>
	 * 
	 * @see DhtConfig#KEY_Q
	 */
	private final DhtConfig.QType q;
	/**
	 * <p>请求参数</p>
	 * 
	 * @see DhtConfig#KEY_A
	 */
	private final Map<String, Object> a;
	/**
	 * <p>请求时间戳</p>
	 */
	private final long timestamp;
	/**
	 * <p>响应</p>
	 */
	private DhtResponse response;
	
	/**
	 * <p>创建请求</p>
	 * <p>生成NodeId</p>
	 * 
	 * @param q 请求类型
	 */
	protected DhtRequest(DhtConfig.QType q) {
		this(DhtContext.getInstance().buildRequestId(), DhtConfig.KEY_Q, q, new LinkedHashMap<>());
		this.put(DhtConfig.KEY_ID, NodeContext.getInstance().nodeId());
	}
	
	/**
	 * <p>解析请求</p>
	 * <p>不生成NodeId</p>
	 * 
	 * @param t 消息ID
	 * @param y 消息类型
	 * @param q 请求类型
	 * @param a 请求参数
	 */
	private DhtRequest(byte[] t, String y, DhtConfig.QType q, Map<String, Object> a) {
		super(t, y);
		this.q = q;
		this.a = a;
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * <p>读取请求</p>
	 * 
	 * @param decoder 消息
	 * 
	 * @return 请求
	 */
	public static final DhtRequest valueOf(final BEncodeDecoder decoder) {
		final byte[] t = decoder.getBytes(DhtConfig.KEY_T);
		final String y = decoder.getString(DhtConfig.KEY_Y);
		final String q = decoder.getString(DhtConfig.KEY_Q);
		final QType type = DhtConfig.QType.of(q);
		final Map<String, Object> a = decoder.getMap(DhtConfig.KEY_A);
		return new DhtRequest(t, y, type, a);
	}
	
	/**
	 * <p>获取请求类型</p>
	 * 
	 * @return 请求类型
	 */
	public QType getQ() {
		return q;
	}

	/**
	 * <p>获取请求参数</p>
	 * 
	 * @return 请求参数
	 */
	public Map<String, Object> getA() {
		return a;
	}
	
	/**
	 * <p>获取请求时间戳</p>
	 * 
	 * @return 请求时间戳
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * <p>获取响应</p>
	 * 
	 * @return 响应
	 */
	public DhtResponse getResponse() {
		return response;
	}

	/**
	 * <p>设置响应</p>
	 * 
	 * @param response 响应
	 */
	public void setResponse(DhtResponse response) {
		this.response = response;
	}

	/**
	 * <p>判断是否已经获取响应</p>
	 * 
	 * @return 是否已经获取响应
	 */
	public boolean hasResponse() {
		return this.response != null;
	}

	@Override
	public final Object get(String key) {
		if(this.a == null) {
			return null;
		}
		return this.a.get(key);
	}
	
	@Override
	public final void put(String key, Object value) {
		this.a.put(key, value);
	}
	
	@Override
	public final byte[] toBytes() {
		final Map<String, Object> request = new LinkedHashMap<>();
		request.put(DhtConfig.KEY_T, this.t);
		request.put(DhtConfig.KEY_Y, this.y);
		request.put(DhtConfig.KEY_Q, this.q.value());
		request.put(DhtConfig.KEY_A, this.a);
		return BEncodeEncoder.encodeMap(request);
	}
	
	/**
	 * <p>序列化节点列表</p>
	 * 
	 * @param nodes 节点列表
	 * 
	 * @return 节点数据
	 */
	protected static final byte[] serializeNodes(List<NodeSession> nodes) {
		if(CollectionUtils.isEmpty(nodes)) {
			return new byte[0];
		}
		final List<NodeSession> availableNodes = nodes.stream()
			.filter(node -> NetUtils.ipAddress(node.getHost())) // 只分享IP地址
			.collect(Collectors.toList());
		if(CollectionUtils.isEmpty(availableNodes)) {
			return new byte[0];
		}
		final ByteBuffer buffer = ByteBuffer.allocate(26 * availableNodes.size()); // 20 + 4 + 2
		for (NodeSession node : availableNodes) {
			buffer.put(node.getId());
			buffer.putInt(NetUtils.ipToInt(node.getHost()));
			buffer.putShort(NetUtils.portToShort(node.getPort()));
		}
		return buffer.array();
	}
	
	/**
	 * <p>添加响应锁</p>
	 */
	public void lockResponse() {
		if(!this.hasResponse()) {
			synchronized (this) {
				if(!this.hasResponse()) {
					try {
						this.wait(DhtConfig.DHT_TIMEOUT);
					} catch (InterruptedException e) {
						LOGGER.debug("线程等待异常", e);
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}
	
	/**
	 * <p>释放响应锁</p>
	 */
	public void unlockResponse() {
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.t);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof DhtRequest) {
			final DhtRequest request = (DhtRequest) object;
			return ArrayUtils.equals(this.t, request.t);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.t, this.y, this.q, this.a);
	}
	
}

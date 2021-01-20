package cn.novelweb.tool.download.snail.config;

import cn.novelweb.tool.download.snail.context.NodeContext;
import cn.novelweb.tool.download.snail.net.torrent.dht.request.AnnouncePeerRequest;
import cn.novelweb.tool.download.snail.net.torrent.dht.request.FindNodeRequest;
import cn.novelweb.tool.download.snail.net.torrent.dht.request.GetPeersRequest;
import cn.novelweb.tool.download.snail.net.torrent.dht.request.PingRequest;
import cn.novelweb.tool.download.snail.pojo.session.NodeSession;
import cn.novelweb.tool.download.snail.utils.FileUtils;
import cn.novelweb.tool.download.snail.utils.NumberUtils;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * <p>DHT节点配置</p>
 *
 * @author acgist
 */
public final class DhtConfig extends PropertiesConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DhtConfig.class);

    /**
     * <p>单例对象</p>
     */
    private static final DhtConfig INSTANCE = new DhtConfig();

    /**
     * <p>获取单例对象</p>
     *
     * @return 单例对象
     */
    public static final DhtConfig getInstance() {
        return INSTANCE;
    }

    /**
     * <p>配置文件：{@value}</p>
     */
    private static final String DHT_CONFIG = "/config/bt.dht.properties";
    /**
     * <p>消息ID：{@value}</p>
     * <p>请求ID、响应ID（默认两个字节）</p>
     */
    public static final String KEY_T = "t";
    /**
     * <p>消息类型：{@value}</p>
     * <p>请求消息类型：{@link #KEY_Q}</p>
     * <p>响应消息类型：{@link #KEY_R}</p>
     */
    public static final String KEY_Y = "y";
    /**
     * <p>请求消息类型、请求类型：{@value}</p>
     * <p>请求消息类型：{@link #KEY_Y}</p>
     * <p>请求类型：{@link QType}</p>
     *
     * @see QType
     */
    public static final String KEY_Q = "q";
    /**
     * <p>响应消息类型、响应参数：{@value}</p>
     * <p>响应消息类型：{@link #KEY_Y}</p>
     * <p>响应参数类型：{@link Map}</p>
     */
    public static final String KEY_R = "r";
    /**
     * <p>请求参数：{@value}</p>
     * <p>请求参数类型：{@link Map}</p>
     */
    public static final String KEY_A = "a";
    /**
     * <p>响应错误：{@value}</p>
     * <p>响应错误类型：{@link Map}</p>
     *
     * @see ErrorCode
     */
    public static final String KEY_E = "e";
    /**
     * <p>客户端版本：{@value}</p>
     */
    public static final String KEY_V = "v";
    /**
     * <p>NodeId：{@value}</p>
     *
     * @see NodeContext#nodeId()
     */
    public static final String KEY_ID = "id";
    /**
     * <p>下载端口：{@value}</p>
     *
     * @see QType#ANNOUNCE_PEER
     * @see SystemConfig#getTorrentPortExt()
     */
    public static final String KEY_PORT = "port";
    /**
     * <p>Token：{@value}</p>
     *
     * @see QType#ANNOUNCE_PEER
     */
    public static final String KEY_TOKEN = "token";
    /**
     * <p>节点列表：{@value}</p>
     *
     * @see QType#FIND_NODE
     * @see QType#GET_PEERS
     */
    public static final String KEY_NODES = "nodes";
    /**
     * <p>Peer列表：{@value}</p>
     *
     * @see QType#GET_PEERS
     */
    public static final String KEY_VALUES = "values";
    /**
     * <p>目标：{@value}</p>
     * <p>NodeId、InfoHash</p>
     *
     * @see QType#FIND_NODE
     */
    public static final String KEY_TARGET = "target";
    /**
     * <p>InfoHash：{@value}</p>
     *
     * @see QType#GET_PEERS
     * @see QType#ANNOUNCE_PEER
     */
    public static final String KEY_INFO_HASH = "info_hash";
    /**
     * <p>是否自动获取端口：{@value}</p>
     *
     * @see #IMPLIED_PORT_AUTO
     * @see #IMPLIED_PORT_CONFIG
     * @see QType#ANNOUNCE_PEER
     */
    public static final String KEY_IMPLIED_PORT = "implied_port";
    /**
     * <p>自动配置（忽略端口配置）</p>
     * <p>使用UDP连接端口作为对等端口并支持uTP</p>
     */
    public static final Integer IMPLIED_PORT_AUTO = 1;
    /**
     * <p>端口配置</p>
     * <p>使用消息端口配置</p>
     *
     * @see #KEY_PORT
     */
    public static final Integer IMPLIED_PORT_CONFIG = 0;
    /**
     * <p>Peer列表长度：{@value}</p>
     *
     * @see QType#GET_PEERS
     */
    public static final int GET_PEER_SIZE = 32;
    /**
     * <p>NodeId长度：{@value}</p>
     */
    public static final int NODE_ID_LENGTH = 20;
    /**
     * <p>Node最大保存数量：{@value}</p>
     * <p>超过Node最大保存数量均匀剔除多余节点</p>
     */
    public static final int MAX_NODE_SIZE = 1024;
    /**
     * <p>DHT请求清理周期（分钟）：{@value}</p>
     */
    public static final int DHT_REQUEST_CLEAN_INTERVAL = 10;
    /**
     * <p>DHT响应超时：{@value}</p>
     */
    public static final int DHT_TIMEOUT = SystemConfig.RECEIVE_TIMEOUT_MILLIS;

    static {
        LOGGER.debug("初始化DHT节点配置：{}", DHT_CONFIG);
        INSTANCE.init();
        INSTANCE.release();
    }

    /**
     * <p>DHT请求类型</p>
     *
     * @author acgist
     */
    public enum QType {

        /**
         * <p>ping</p>
         *
         * @see PingRequest
         */
        PING("ping"),
        /**
         * <p>查找节点</p>
         *
         * @see FindNodeRequest
         */
        FIND_NODE("find_node"),
        /**
         * <p>查找Peer</p>
         *
         * @see GetPeersRequest
         */
        GET_PEERS("get_peers"),
        /**
         * <p>声明Peer</p>
         *
         * @see AnnouncePeerRequest
         */
        ANNOUNCE_PEER("announce_peer");

        /**
         * <p>类型标识</p>
         */
        private final String value;

        /**
         * @param value 类型标识
         */
        private QType(String value) {
            this.value = value;
        }

        /**
         * <p>获取类型标识</p>
         *
         * @return 类型标识
         */
        public String value() {
            return this.value;
        }

        /**
         * <p>通过类型标识获取请求类型</p>
         *
         * @param value 类型标识
         * @return 请求类型
         */
        public static final QType of(String value) {
            final DhtConfig.QType[] types = QType.values();
            for (QType type : types) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return null;
        }

    }

    /**
     * <p>DHT响应错误</p>
     * <p>数据格式：{@link List}</p>
     * <p>信息格式：[0]=错误编码；[1]=错误描述；</p>
     *
     * @author acgist
     */
    public enum ErrorCode {

        /**
         * <p>一般错误</p>
         */
        CODE_201(201),
        /**
         * <p>服务错误</p>
         */
        CODE_202(202),
        /**
         * <p>协议错误：不规范包、无效参数、错误Token</p>
         */
        CODE_203(203),
        /**
         * <p>未知方法</p>
         */
        CODE_204(204);

        /**
         * <p>错误编码</p>
         */
        private final int code;

        /**
         * @param code 错误编码
         */
        private ErrorCode(int code) {
            this.code = code;
        }

        /**
         * <p>获取错误编码</p>
         *
         * @return 错误编码
         */
        public int code() {
            return this.code;
        }

    }

    /**
     * <p>默认DHT节点</p>
     * <p>NodeID=host:port</p>
     */
    private final Map<String, String> nodes = new LinkedHashMap<>();

    /**
     * <p>禁止创建实例</p>
     */
    private DhtConfig() {
        super(DHT_CONFIG);
    }

    /**
     * <p>初始化配置</p>
     */
    private void init() {
        this.properties.entrySet().forEach(entry -> {
            final String nodeId = (String) entry.getKey();
            final String address = (String) entry.getValue();
            if (StringUtils.isNotEmpty(nodeId) && StringUtils.isNotEmpty(address)) {
                this.nodes.put(nodeId, address);
            } else {
                LOGGER.warn("默认DHT节点注册失败：{}-{}", nodeId, address);
            }
        });
    }

    /**
     * <p>获取所有DHT节点</p>
     *
     * @return 所有DHT节点
     */
    public Map<String, String> nodes() {
        return this.nodes;
    }

    /**
     * <p>保存DHT节点配置</p>
     * <p>注意：如果没有启动BT任务没有必要保存</p>
     */
    public void persistent() {
        LOGGER.debug("保存DHT节点配置");
        final List<NodeSession> persistentNodes = NodeContext.getInstance().nodes();
        final int size = persistentNodes.size();
        final Random random = NumberUtils.random();
        final Map<String, String> data = persistentNodes.stream()
                .filter(NodeSession::persistentable)
				// 随机保存
                .filter(node -> size < MAX_NODE_SIZE || random.nextInt(size) < MAX_NODE_SIZE)
                .collect(Collectors.toMap(
                        node -> StringUtils.hex(node.getId()),
                        node -> node.getHost() + ":" + node.getPort()
                ));
        this.persistent(data, FileUtils.userDirFile(DHT_CONFIG));
    }

}

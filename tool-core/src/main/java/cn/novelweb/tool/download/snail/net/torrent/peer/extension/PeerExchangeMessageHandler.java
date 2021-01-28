package cn.novelweb.tool.download.snail.net.torrent.peer.extension;

import cn.novelweb.tool.download.snail.config.PeerConfig;
import cn.novelweb.tool.download.snail.config.PeerConfig.ExtensionType;
import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.PeerContext;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.context.exception.PacketSizeException;
import cn.novelweb.tool.download.snail.format.BEncodeDecoder;
import cn.novelweb.tool.download.snail.format.BEncodeEncoder;
import cn.novelweb.tool.download.snail.net.torrent.peer.ExtensionMessageHandler;
import cn.novelweb.tool.download.snail.net.torrent.peer.ExtensionTypeMessageHandler;
import cn.novelweb.tool.download.snail.pojo.session.PeerSession;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.utils.CollectionUtils;
import cn.novelweb.tool.download.snail.utils.MapUtils;
import cn.novelweb.tool.download.snail.utils.NetUtils;
import cn.novelweb.tool.download.snail.utils.PeerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Peer Exchange (PEX)</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0011.html</p>
 *
 * @author acgist
 */
public final class PeerExchangeMessageHandler extends ExtensionTypeMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerExchangeMessageHandler.class);

    //================IPv4================//
    /**
     * <p>地址：{@value}</p>
     */
    private static final String ADDED = "added";
    /**
     * <p>属性：{@value}</p>
     */
    private static final String ADDEDF = "added.f";
    /**
     * <p>删除地址：{@value}</p>
     */
    private static final String DROPPED = "dropped";

    //================IPv6================//
    /**
     * <p>地址：{@value}</p>
     */
    private static final String ADDED6 = "added6";
    /**
     * <p>属性：{@value}</p>
     */
    private static final String ADDED6F = "added6.f";
    /**
     * <p>删除地址：{@value}</p>
     */
    private static final String DROPPED6 = "dropped6";

    /**
     * <p>BT任务信息</p>
     */
    private final TorrentSession torrentSession;

    /**
     * @param peerSession             Peer信息
     * @param torrentSession          BT任务信息
     * @param extensionMessageHandler 扩展协议代理
     */
    private PeerExchangeMessageHandler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
        super(ExtensionType.UT_PEX, peerSession, extensionMessageHandler);
        this.torrentSession = torrentSession;
    }

    /**
     * <p>创建PEX扩展协议代理</p>
     *
     * @param peerSession             Peer
     * @param torrentSession          BT任务信息
     * @param extensionMessageHandler 扩展消息代理
     * @return PEX扩展协议代理
     */
    public static PeerExchangeMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
        return new PeerExchangeMessageHandler(peerSession, torrentSession, extensionMessageHandler);
    }

    @Override
    public void doMessage(ByteBuffer buffer) throws NetException {
        this.pex(buffer);
    }

    /**
     * <p>发送消息：pex</p>
     *
     * @param bytes 消息
     */
    public void pex(byte[] bytes) {
        LOGGER.debug("发送pex消息");
        this.pushMessage(bytes);
    }

    /**
     * <p>处理消息：pex</p>
     *
     * @param buffer 消息
     * @throws PacketSizeException 网络包异常
     *                             <p>
     *                             TODO：IPv6
     */
    private void pex(ByteBuffer buffer) throws PacketSizeException {
        LOGGER.debug("处理pex消息");
        final BEncodeDecoder decoder = BEncodeDecoder.newInstance(buffer);
        decoder.nextMap();
        if (decoder.isEmpty()) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("处理pex消息错误（格式）：{}", decoder.oddString());
            }
            return;
        }
        final byte[] added = decoder.getBytes(ADDED);
        final byte[] addedf = decoder.getBytes(ADDEDF);
        final Map<String, Integer> peers = PeerUtils.read(added);
        if (MapUtils.isNotEmpty(peers)) {
            final AtomicInteger index = new AtomicInteger(0);
            peers.forEach((host, port) -> {
                final PeerSession peerSession = PeerContext.getInstance().newPeerSession(
                        this.torrentSession.infoHashHex(),
                        this.torrentSession.statistics(),
                        host,
                        port,
                        PeerConfig.Source.PEX
                );
                if (addedf != null && addedf.length > index.get()) {
                    peerSession.flags(addedf[index.getAndIncrement()]);
                }
                peerSession.pexSource(this.peerSession); // 设置Pex来源
            });
        }
    }

    /**
     * <p>创建pex消息</p>
     *
     * @param optimize 优质Peer列表
     * @return 消息
     * <p>
     * TODO：IPv6
     */
    public static byte[] buildMessage(List<PeerSession> optimize) {
        if (CollectionUtils.isEmpty(optimize)) {
            return new byte[0];
        }
        final int length = SystemConfig.IP_PORT_LENGTH * optimize.size();
        final ByteBuffer addedBuffer = ByteBuffer.allocate(length);
        final ByteBuffer addedfBuffer = ByteBuffer.allocate(optimize.size());
        optimize.stream()
                .distinct()
                .forEach(session -> {
                    addedBuffer.putInt(NetUtils.ipToInt(session.host()));
                    addedBuffer.putShort(NetUtils.portToShort(session.port()));
                    addedfBuffer.put(session.flags());
                });
        final Map<String, Object> data = new HashMap<>(9);
        final byte[] emptyBytes = new byte[0];
        data.put(ADDED, addedBuffer.array());
        data.put(ADDEDF, addedfBuffer.array());
        data.put(DROPPED, emptyBytes);
        data.put(ADDED6, emptyBytes);
        data.put(ADDED6F, emptyBytes);
        data.put(DROPPED6, emptyBytes);
        return BEncodeEncoder.encodeMap(data);
    }

}

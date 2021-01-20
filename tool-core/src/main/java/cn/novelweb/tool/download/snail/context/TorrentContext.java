package cn.novelweb.tool.download.snail.context;

import cn.novelweb.tool.download.snail.IContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.format.BEncodeDecoder;
import cn.novelweb.tool.download.snail.format.BEncodeEncoder;
import cn.novelweb.tool.download.snail.pojo.bean.InfoHash;
import cn.novelweb.tool.download.snail.pojo.bean.Torrent;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>Torrent上下文</p>
 *
 * @author acgist
 */
public final class TorrentContext implements IContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(TorrentContext.class);

    private static final TorrentContext INSTANCE = new TorrentContext();

    public static final TorrentContext getInstance() {
        return INSTANCE;
    }

    /**
     * <p>BT任务MAP</p>
     * <p>InfoHashHex=BT任务信息</p>
     */
    private final Map<String, TorrentSession> torrentSessions;

    private TorrentContext() {
        this.torrentSessions = new ConcurrentHashMap<>();
    }

    /**
     * <p>获取所有的InfoHash拷贝</p>
     *
     * @return 所有的InfoHash拷贝
     */
    public List<InfoHash> allInfoHash() {
        return this.torrentSessions.values().stream()
                .map(session -> session.infoHash())
                .collect(Collectors.toList());
    }

    /**
     * <p>获取所有的TorrentSession拷贝</p>
     *
     * @return 所有的TorrentSession拷贝
     */
    public List<TorrentSession> allTorrentSession() {
        return this.torrentSessions.values().stream()
                .collect(Collectors.toList());
    }

    /**
     * <p>获取BT任务信息</p>
     *
     * @param infoHashHex InfoHashHex
     * @return BT任务信息
     */
    public TorrentSession torrentSession(String infoHashHex) {
        return this.torrentSessions.get(infoHashHex);
    }

    /**
     * <p>删除TorrentSession</p>
     *
     * @param infoHashHex InfoHashHex
     */
    public void remove(String infoHashHex) {
        LOGGER.debug("删除种子信息：{}", infoHashHex);
        this.torrentSessions.remove(infoHashHex);
    }

    /**
     * <p>判断是否存在下载任务</p>
     *
     * @param infoHashHex InfoHashHex
     * @return 任务是否存在
     */
    public boolean exist(String infoHashHex) {
        return this.torrentSessions.containsKey(infoHashHex);
    }

    /**
     * <p>新建TorrentSession</p>
     * <p>如果已存在InfoHashHex：直接返回</p>
     * <p>如果不存在InfoHashHex：优先使用path加载，如果path为空时使用InfoHashHex加载。</p>
     * <p>使用InfoHashHex加载的BT任务信息用于磁力链接下载</p>
     *
     * @param infoHashHex InfoHashHex
     * @param path        种子文件路径
     * @return BT任务信息
     * @throws DownloadException 下载异常
     */
    public TorrentSession newTorrentSession(String infoHashHex, String path) throws DownloadException {
        final TorrentSession session = this.torrentSession(infoHashHex);
        if (session != null) {
            return session;
        }
        if (StringUtils.isEmpty(path)) {
            return this.newTorrentSession(InfoHash.newInstance(infoHashHex), null);
        } else {
            return this.newTorrentSession(path);
        }
    }

    /**
     * <p>新建TorrentSession</p>
     *
     * @param path 种子文件路径
     * @return BT任务信息
     * @throws DownloadException 下载异常
     */
    public TorrentSession newTorrentSession(String path) throws DownloadException {
        final Torrent torrent = loadTorrent(path);
        return this.newTorrentSession(torrent.infoHash(), torrent);
    }

    /**
     * <p>新建TorrentSession</p>
     *
     * @param infoHash InfoHash
     * @param torrent  种子信息
     * @return BT任务信息
     * @throws DownloadException 下载异常
     */
    private TorrentSession newTorrentSession(InfoHash infoHash, Torrent torrent) throws DownloadException {
        if (infoHash == null) {
            throw new DownloadException("创建TorrentSession失败（InfoHash为空）");
        }
        final String infoHashHex = infoHash.infoHashHex();
        TorrentSession torrentSession = this.torrentSessions.get(infoHashHex);
        if (torrentSession == null) {
            torrentSession = TorrentSession.newInstance(infoHash, torrent);
            this.torrentSessions.put(infoHashHex, torrentSession);
        }
        return torrentSession;
    }

    /**
     * <p>种子文件加载</p>
     *
     * @param path 种子文件地址
     * @return 种子信息
     * @throws DownloadException 下载异常
     */
    public static final Torrent loadTorrent(String path) throws DownloadException {
        final File file = new File(path);
        if (!file.exists()) {
            throw new DownloadException("种子文件不存在");
        }
        try {
            final byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            final BEncodeDecoder decoder = BEncodeDecoder.newInstance(bytes);
            decoder.nextMap();
            if (decoder.isEmpty()) {
                throw new DownloadException("种子文件格式错误");
            }
            final Torrent torrent = Torrent.valueOf(decoder);
            // 直接转储原始信息：防止顺序不对导致种子Hash计算错误
            final Map<String, Object> info = decoder.getMap("info");
            final InfoHash infoHash = InfoHash.newInstance(BEncodeEncoder.encodeMap(info));
            torrent.infoHash(infoHash);
            return torrent;
        } catch (DownloadException e) {
            throw e;
        } catch (NetException | IOException e) {
            throw new DownloadException("种子文件加载失败", e);
        }
    }

}

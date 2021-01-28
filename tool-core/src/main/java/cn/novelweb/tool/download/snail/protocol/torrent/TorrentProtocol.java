package cn.novelweb.tool.download.snail.protocol.torrent;

import cn.hutool.core.io.FileUtil;
import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.context.TorrentContext;
import cn.novelweb.tool.download.snail.context.exception.DownloadException;
import cn.novelweb.tool.download.snail.downloader.IDownloader;
import cn.novelweb.tool.download.snail.downloader.torrent.TorrentDownloader;
import cn.novelweb.tool.download.snail.pojo.ITaskSession;
import cn.novelweb.tool.download.snail.pojo.ITaskSession.FileType;
import cn.novelweb.tool.download.snail.pojo.bean.Torrent;
import cn.novelweb.tool.download.snail.pojo.session.TaskSession;
import cn.novelweb.tool.download.snail.pojo.session.TorrentSession;
import cn.novelweb.tool.download.snail.protocol.Protocol;
import cn.novelweb.tool.download.snail.utils.FileUtils;

/**
 * <p>BT协议</p>
 *
 * @author acgist
 */
public final class TorrentProtocol extends Protocol {

    private static final TorrentProtocol INSTANCE = new TorrentProtocol();

    public static TorrentProtocol getInstance() {
        return INSTANCE;
    }

    /**
     * <p>种子文件操作类型</p>
     *
     * @author acgist
     */
    public enum TorrentHandle {

        /**
         * 拷贝：拷贝种子文件到下载目录（源文件不变）
         */
        COPY,
        /**
         * 移动：移动种子文件到下载目录（源文件删除）
         */
        MOVE;

    }

    /**
     * <p>种子文件路径</p>
     */
    private String torrentFile;
    /**
     * <p>种子信息</p>
     */
    private TorrentSession torrentSession;
    /**
     * <p>种子文件操作类型</p>
     */
    private TorrentHandle handle = TorrentHandle.COPY;

    private TorrentProtocol() {
        super(Type.TORRENT);
    }

    /**
     * <p>设置种子文件操作类型</p>
     *
     * @param handle 种子文件操作
     */
    public void torrentHandle(TorrentHandle handle) {
        this.handle = handle;
    }

    @Override
    public String name() {
        return "BitTorrent";
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public IDownloader buildDownloader(ITaskSession taskSession) {
        return TorrentDownloader.newInstance(taskSession);
    }

    @Override
    protected void prep() throws DownloadException {
        this.exist();
        this.torrent();
    }

    @Override
    protected String buildFileName() {
        return this.torrentSession.name();
    }

    @Override
    protected void buildName(String fileName) {
        this.taskEntity.setName(fileName);
    }

    @Override
    protected void buildFileType(String fileName) {
        this.taskEntity.setFileType(FileType.TORRENT);
    }

    @Override
    protected void buildSize() throws DownloadException {
        // 设置选择下载文件时计算大小
    }

    @Override
    protected void done() throws DownloadException {
        this.buildFolder();
        this.torrentHandle();
        this.selectTorrentFiles();
    }

    /**
     * {@inheritDoc}
     *
     * <p>注意：一定先检查BT任务是否已经存在（如果已经存在不能赋值：防止清除已下载任务）</p>
     */
    @Override
    protected void release(boolean success) {
        super.release(success);
        if (!success) {
            // 清除种子信息
            if (this.torrentSession != null) {
                TorrentContext.getInstance().remove(this.torrentSession.infoHashHex());
            }
        }
        this.torrentFile = null;
        this.torrentSession = null;
    }

    /**
     * <p>判断任务是否已经存在</p>
     *
     * @throws DownloadException 下载异常
     */
    private void exist() throws DownloadException {
        final Torrent torrent = TorrentContext.loadTorrent(this.url);
        if (TorrentContext.getInstance().exist(torrent.infoHash().infoHashHex())) {
            throw new DownloadException("任务已经存在");
        }
    }

    /**
     * <p>解析种子</p>
     * <p>转换磁力链接、生成种子信息</p>
     *
     * @throws DownloadException 下载异常
     */
    private void torrent() throws DownloadException {
        final String torrentFile = this.url;
        final TorrentSession torrentSession = TorrentContext.getInstance().newTorrentSession(torrentFile);
        this.url = Protocol.Type.buildMagnet(torrentSession.infoHash().infoHashHex()); // 生成磁力链接
        this.torrentFile = torrentFile;
        this.torrentSession = torrentSession;
    }

    /**
     * <p>创建下载目录</p>
     */
    private void buildFolder() {
        FileUtils.buildFolder(this.taskEntity.getFile(), false);
    }

    /**
     * <p>种子文件操作：拷贝、移动</p>
     */
    private void torrentHandle() {
        final String fileName = FileUtils.fileName(this.torrentFile);
        final String newFilePath = FileUtils.file(this.taskEntity.getFile(), fileName);
        if (this.handle == TorrentHandle.MOVE) {
            FileUtils.move(this.torrentFile, newFilePath);
        } else {
            FileUtil.copy(this.torrentFile, newFilePath, true);
        }
        this.taskEntity.setTorrent(newFilePath);
    }

    /**
     * <p>选择下载文件、设置文件大小</p>
     *
     * @throws DownloadException 下载异常
     */
    private void selectTorrentFiles() throws DownloadException {
        ITaskSession taskSession = null;
        try {
            taskSession = TaskSession.newInstance(this.taskEntity);
            GuiContext.getInstance().torrent(taskSession);
        } catch (DownloadException e) {
            throw e;
        } catch (Exception e) {
            throw new DownloadException("选择下载文件错误", e);
        }
        if (taskSession.multifileSelected().isEmpty()) {
            // 没有选择下载文件：删除已经创建文件
            FileUtils.delete(this.taskEntity.getFile());
            throw new DownloadException("请选择下载文件");
        }
    }

}

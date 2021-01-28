package cn.novelweb.tool.download.torrent.client;

import cn.novelweb.tool.download.snail.Snail;
import cn.novelweb.tool.download.snail.context.GuiContext;
import cn.novelweb.tool.download.snail.context.TorrentContext;
import cn.novelweb.tool.download.snail.gui.event.adapter.TorrentEventAdapter;
import cn.novelweb.tool.download.snail.pojo.bean.Torrent;
import cn.novelweb.tool.download.snail.pojo.bean.TorrentFile;
import cn.novelweb.tool.download.snail.pojo.wrapper.MultifileSelectorWrapper;
import cn.novelweb.tool.download.snail.utils.Performance;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p></p>
 * <p>2021-01-15 18:54</p>
 *
 * @author Dan
 **/
public class SimpleClientTest extends Performance {

    // 实现种子选择事件
    private static final class TorrentEvent extends TorrentEventAdapter {
    }


    @Test
    @SneakyThrows
    public void downloadTest() {
//        TorrentMetadata t2 = new TorrentParser().parseFromFile(new File("C:\\Users\\12240\\Desktop\\kb3.torrent"));
//        final List<TorrentFile> tmpFileNames = t2.getFiles();
//        for (TorrentFile torrentFileInfo : tmpFileNames) {
//            System.out.println(t2.getDirectoryName() + "/" + torrentFileInfo.getRelativePathAsString().replaceAll("\\\\", "/"));
//        }

//        final Snail snail = Snail.SnailBuilder.newBuilder().enableAllProtocol().buildSync();
//        snail.download("C:\\Users\\12240\\Desktop\\kb3.torrent");
//        snail.lockDownload();

//        final String url = "C:\\Users\\12240\\Desktop\\kb3.torrent";
//        ProtocolContext.getInstance().register(TorrentProtocol.getInstance()).available(true);
//        final Torrent torrent = TorrentContext.loadTorrent(url);
//        final List<String> files = torrent.getInfo().files().stream()
//                .map(TorrentFile::path)
//                .collect(Collectors.toList());
//        final List<String> list = new ArrayList<>();
//        // 选择下载文件
//        files.forEach(file -> {
//            System.out.println(file);
//            if(!file.contains(TorrentInfo.PADDING_FILE_PREFIX)) {
//                if(file.contains("本片简介，海报，截图等，双击进入查看.url")) {
//                    list.add(file);
//                }
//            }
//        });
//        MultifileSelectorWrapper wrapper = MultifileSelectorWrapper.newEncoder(list);
//        final ITaskSession taskSession = TorrentProtocol.getInstance().buildTaskSession(url);
//
//        final TaskEntity entity = new TaskEntity();
//        entity.setFile("C://Users/12240/Desktop/directory/"); // 设置下载路径
//        entity.setType(Protocol.Type.TORRENT); // 设置下载类型
//        entity.setDescription(wrapper.serialize()); // 设置下载文件
//        torrentSession.upload(TaskSession.newInstance(entity)).download(false); // 禁止自动加载Peer


//        final IDownloader downloader = taskSession.buildDownloader();
//		downloader.run(); // 不下载
//        System.out.println(taskSession.getFile());

//        final Snail snail = Snail.SnailBuilder.newBuilder()
//                .enableAllProtocol()
//                .buildSync();
//        snail.download("C:\\Users\\12240\\Desktop\\kb3.torrent");
//        snail.lockDownload();


        final Snail snail = Snail.SnailBuilder.newBuilder()
                .enableTorrent()
                .buildSync();
        // 注册种子选择事件
        GuiContext.register(new TorrentEventAdapter());
        // 解析种子文件
        final Torrent torrent = TorrentContext.loadTorrent("C:\\Users\\12240\\Desktop\\[pianku.tv][BeanSub.torrent");
        // 自行过滤下载文件
        final List<String> list = torrent.getInfo().files().stream()
                .filter(TorrentFile::isNotPaddingFile)
                .map(TorrentFile::path)
                .collect(Collectors.toList());
        // 设置需要下载文件
        GuiContext.getInstance().files(MultifileSelectorWrapper.newEncoder(list).serialize());
        // 开始下载
        snail.download("C:\\Users\\12240\\Desktop\\[pianku.tv][BeanSub.torrent");
        snail.lockDownload();

//        EntityInitializer.newInstance().sync(); // 初始化实体
//        final String path = "C:\\Users\\12240\\Desktop\\kb3.torrent"; // 种子文件
//        final TorrentSession torrentSession = TorrentContext.getInstance().newTorrentSession(path);
//        final List<TorrentFile> files = torrentSession.torrent().getInfo().files();
//        final List<String> list = new ArrayList<>();
//        // 选择下载文件
//        files.forEach(file -> {
//            if(!file.path().contains(TorrentInfo.PADDING_FILE_PREFIX)) {
//                System.out.println(file.path()+":"+file.getLength());
//                if(file.path().contains("梦幻天堂·龙网最新超多爆爽资源下载列表.rtf")) {
//                    list.add(file.path());
//                }
//            }
//        });
//        if (list.isEmpty()) {
//            return;
//        }
//        final MultifileSelectorWrapper wrapper = MultifileSelectorWrapper.newEncoder(list);
//        final TaskEntity entity = new TaskEntity();
//        entity.setFile("C://Users/12240/Desktop/directory/"); // 设置下载路径
//        entity.setType(Protocol.Type.TORRENT); // 设置下载类型
//        entity.setDescription(wrapper.serialize()); // 设置下载文件
//        torrentSession.upload(TaskSession.newInstance(entity)).download(false); // 禁止自动加载Peer
//        final String host = "127.0.0.1";
//        final Integer port = 18888;
//        final StatisticsSession statisticsSession = new StatisticsSession(); // 统计
//        final PeerSession peerSession = PeerSession.newInstance(statisticsSession, host, port); // Peer
//        peerSession.flags(PeerConfig.PEX_UTP); // UTP支持
//        final PeerDownloader launcher = PeerDownloader.newInstance(peerSession, torrentSession); // 下载器
//        launcher.handshake();
//        new Thread(() -> {
//            while(true) {
//                LOGGER.debug("下载速度：{}", statisticsSession.downloadSpeed());
//                ThreadUtils.sleep(1000);
//            }
//        }).start();
//        this.pause();


//        final String path = "C:\\Users\\12240\\Desktop\\kb3.torrent";
//        final TorrentSession torrentSession = TorrentContext.getInstance().newTorrentSession(path);
//        final List<TorrentFile> files = torrentSession.torrent().getInfo().files();
//        final List<String> list = new ArrayList<>();
//        files.forEach(file -> {
//            if (!file.path().contains(TorrentInfo.PADDING_FILE_PREFIX)) {
//                System.out.println(file.path() + ":" + file.getLength());
//                if (file.path().contains("梦幻天堂·龙网最新超多爆爽资源下载列表.rtf")) {
//                    list.add(file.path());
//                }
//            }
//        });
//        System.out.println(list.toString());
//        final MultifileSelectorWrapper wrapper = MultifileSelectorWrapper.newEncoder(list);
//        final TaskEntity entity = new TaskEntity();
//        entity.setFile("C://Users/12240/Desktop/directory/"); // 设置下载路径
//        entity.setType(Protocol.Type.TORRENT); // 设置下载类型
//        entity.setDescription(wrapper.serialize()); // 设置下载文件
//        torrentSession.upload(TaskSession.newInstance(entity));
//        final PeerServer server = PeerServer.getInstance(); // Peer服务
//        server.listen();
//        TorrentServer.getInstance();
//        this.pause();


//        final String url = "C:\\Users\\12240\\Desktop\\kb3.torrent";
//        ProtocolContext.getInstance().register(TorrentProtocol.getInstance()).available(true);
//        final Torrent torrent = TorrentContext.loadTorrent(url);
//        final List<String> files = torrent.getInfo().files().stream()
//                .map(TorrentFile::path)
//                .collect(Collectors.toList());
//        final ITaskSession taskSession = TorrentProtocol.getInstance()
//                .buildTaskSession(url);
//
//        final List<String> list = new ArrayList<>();
//        files.forEach(file -> {
//            System.out.println(file);
//            if (!file.contains(TorrentInfo.PADDING_FILE_PREFIX)) {
//                if (file.contains("梦幻天堂·龙网最新超多爆爽资源下载列表.rtf")) {
//                    list.add(file);
//                }
//            }
//        });
//        taskSession.multifileSelected().addAll(list);
//        final IDownloader downloader = taskSession.buildDownloader();
//        downloader.run();
//        final File file = new File(taskSession.getFile());
//        System.out.println(file.length());


    }


}

package cn.novelweb.tool.download.torrent.network;

import java.util.concurrent.ExecutorService;

public interface ConnectionManagerContext extends ChannelListenerFactory {

  ExecutorService getExecutor();

}

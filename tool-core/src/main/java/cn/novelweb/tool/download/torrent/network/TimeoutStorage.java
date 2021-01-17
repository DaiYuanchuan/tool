package cn.novelweb.tool.download.torrent.network;

import java.util.concurrent.TimeUnit;

public interface TimeoutStorage {

  void setTimeout(long millis);

  void setTimeout(int timeout, TimeUnit timeUnit);

  long getTimeoutMillis();

}

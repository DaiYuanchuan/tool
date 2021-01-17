package cn.novelweb.tool.download.torrent.common;

public class SystemTimeService implements TimeService {

  @Override
  public long now() {
    return System.currentTimeMillis();
  }
}

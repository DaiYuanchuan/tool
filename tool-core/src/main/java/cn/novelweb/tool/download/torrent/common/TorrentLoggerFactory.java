package cn.novelweb.tool.download.torrent.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

public final class TorrentLoggerFactory {

  @Nullable
  private static volatile String staticLoggersName = null;

  public static Logger getLogger(Class<?> clazz) {
    String name = staticLoggersName;
    if (name == null) {
      name = clazz.getName();
    }
    return LoggerFactory.getLogger(name);
  }

  public static void setStaticLoggersName(@Nullable String staticLoggersName) {
    TorrentLoggerFactory.staticLoggersName = staticLoggersName;
  }
}

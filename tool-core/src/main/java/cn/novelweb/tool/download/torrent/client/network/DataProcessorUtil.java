package cn.novelweb.tool.download.torrent.client.network;

import cn.novelweb.tool.download.torrent.common.LoggerUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.channels.ByteChannel;

public final class DataProcessorUtil {

  public static void closeChannelIfOpen(Logger logger, ByteChannel channel) {
    if (channel.isOpen()) {
      logger.trace("close channel {}", channel);
      try {
        channel.close();
      } catch (IOException e) {
        LoggerUtils.errorAndDebugDetails(logger, "unable to close channel {}", channel, e);
      }
    }
  }
}

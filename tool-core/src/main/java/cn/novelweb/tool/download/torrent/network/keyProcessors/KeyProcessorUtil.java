package cn.novelweb.tool.download.torrent.network.keyProcessors;

import cn.novelweb.tool.download.torrent.common.TorrentLoggerFactory;
import cn.novelweb.tool.download.torrent.network.TimeoutAttachment;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class KeyProcessorUtil {

  private final static Logger logger = TorrentLoggerFactory.getLogger(KeyProcessorUtil.class);

  public static TimeoutAttachment getAttachmentAsTimeoutOrNull(SelectionKey key) {
    Object attachment = key.attachment();
    if (attachment instanceof TimeoutAttachment) {
      return (TimeoutAttachment) attachment;
    }
    logger.error("unable to cast attachment {} to timeout attachment type", attachment);
    return null;
  }

  public static SocketChannel getCastedChannelOrNull(SelectionKey key) {
    SelectableChannel channel = key.channel();
    if (channel instanceof SocketChannel) {
      return (SocketChannel) channel;
    }
    logger.error("unable to cast channel {} to specified type");
    return null;
  }

  public static void setBuffersSizeIfNecessary(SocketChannel socketChannel, int sendBufferSize, int receiveBufferSize) throws IOException {
    final Socket socket = socketChannel.socket();
    if (sendBufferSize > 0) {
      socket.setSendBufferSize(sendBufferSize);
    }
    if (receiveBufferSize > 0) {
      socket.setReceiveBufferSize(receiveBufferSize);
    }
  }
}

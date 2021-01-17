package cn.novelweb.tool.download.torrent.network.keyProcessors;

import cn.novelweb.tool.download.torrent.common.TorrentLoggerFactory;
import cn.novelweb.tool.download.torrent.network.ConnectionListener;
import cn.novelweb.tool.download.torrent.network.ReadAttachment;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ReadableKeyProcessor implements KeyProcessor {

  private static final Logger logger = TorrentLoggerFactory.getLogger(ReadableKeyProcessor.class);

  private final String myServerSocketLocalAddress;

  public ReadableKeyProcessor(String serverSocketLocalAddress) {
    this.myServerSocketLocalAddress = serverSocketLocalAddress;
  }

  @Override
  public void process(SelectionKey key) throws IOException {
    SelectableChannel channel = key.channel();
    if (!(channel instanceof SocketChannel)) {
      logger.warn("incorrect instance of channel. The key is cancelled");
      key.cancel();
      return;
    }

    SocketChannel socketChannel = (SocketChannel) channel;
    logger.trace("server {} get new data from {}", myServerSocketLocalAddress, socketChannel);

    Object attachment = key.attachment();
    if (!(attachment instanceof ReadAttachment)) {
      logger.warn("incorrect instance of attachment for channel {}", new Object[]{socketChannel.socket()});
      socketChannel.close();
      return;
    }
    ConnectionListener connectionListener = ((ReadAttachment) attachment).getConnectionListener();
    connectionListener.onNewDataAvailable(socketChannel);
  }

  @Override
  public boolean accept(SelectionKey key) {
    return key.isValid() && key.isReadable();
  }
}

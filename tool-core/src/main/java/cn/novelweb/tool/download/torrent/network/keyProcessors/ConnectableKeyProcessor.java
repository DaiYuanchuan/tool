package cn.novelweb.tool.download.torrent.network.keyProcessors;

import cn.novelweb.tool.download.torrent.common.TimeService;
import cn.novelweb.tool.download.torrent.common.TorrentLoggerFactory;
import cn.novelweb.tool.download.torrent.network.ConnectTask;
import cn.novelweb.tool.download.torrent.network.ConnectionListener;
import cn.novelweb.tool.download.torrent.network.ReadWriteAttachment;
import cn.novelweb.tool.download.torrent.network.TimeoutStorage;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectableKeyProcessor implements KeyProcessor {

  private static final Logger logger = TorrentLoggerFactory.getLogger(ConnectableKeyProcessor.class);

  private final Selector mySelector;
  private final TimeService myTimeService;
  private final TimeoutStorage myTimeoutStorage;
  private final AtomicInteger mySendBufferSize;
  private final AtomicInteger myReceiveBufferSize;

  public ConnectableKeyProcessor(Selector selector,
                                 TimeService timeService,
                                 TimeoutStorage timeoutStorage,
                                 AtomicInteger sendBufferSize,
                                 AtomicInteger receiveBufferSize) {
    this.mySelector = selector;
    this.myTimeService = timeService;
    this.myTimeoutStorage = timeoutStorage;
    this.mySendBufferSize = sendBufferSize;
    this.myReceiveBufferSize = receiveBufferSize;
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
    Object attachment = key.attachment();
    if (!(attachment instanceof ConnectTask)) {
      logger.warn("incorrect instance of attachment for channel {}. The key for the channel is cancelled", socketChannel);
      key.cancel();
      return;
    }
    final ConnectTask connectTask = (ConnectTask) attachment;
    final ConnectionListener connectionListener = connectTask.getConnectionListener();
    final boolean isConnectFinished;
    try {
      isConnectFinished = socketChannel.finishConnect();
    } catch (NoRouteToHostException e) {
      logger.info("Could not connect to {}:{}, received NoRouteToHostException", connectTask.getHost(), connectTask.getPort());
      connectionListener.onError(socketChannel, e);
      return;
    } catch (ConnectException e) {
      logger.info("Could not connect to {}:{}, received ConnectException", connectTask.getHost(), connectTask.getPort());
      connectionListener.onError(socketChannel, e);
      return;
    }
    if (!isConnectFinished) {
      logger.info("Could not connect to {}:{}", connectTask.getHost(), connectTask.getPort());
      connectionListener.onError(socketChannel, null);
      return;
    }
    socketChannel.configureBlocking(false);
    KeyProcessorUtil.setBuffersSizeIfNecessary(socketChannel, mySendBufferSize.get(), myReceiveBufferSize.get());
    ReadWriteAttachment keyAttachment = new ReadWriteAttachment(connectionListener, myTimeService.now(), myTimeoutStorage.getTimeoutMillis());
    socketChannel.register(mySelector, SelectionKey.OP_READ, keyAttachment);
    logger.debug("setup new TCP connection with {}", socketChannel);
    connectionListener.onConnectionEstablished(socketChannel);
  }

  @Override
  public boolean accept(SelectionKey key) {
    return key.isValid() && key.isConnectable();
  }
}

package cn.novelweb.tool.download.torrent.network.keyProcessors;

import cn.novelweb.tool.download.torrent.common.TorrentLoggerFactory;
import cn.novelweb.tool.download.torrent.network.ConnectionClosedException;
import cn.novelweb.tool.download.torrent.network.WriteAttachment;
import cn.novelweb.tool.download.torrent.network.WriteTask;
import org.slf4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class WritableKeyProcessor implements KeyProcessor {

  private static final Logger logger = TorrentLoggerFactory.getLogger(WritableKeyProcessor.class);

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
    if (!(attachment instanceof WriteAttachment)) {
      logger.error("incorrect instance of attachment for channel {}", channel);
      key.cancel();
      return;
    }

    WriteAttachment keyAttachment = (WriteAttachment) attachment;

    if (keyAttachment.getWriteTasks().isEmpty()) {
      key.interestOps(SelectionKey.OP_READ);
      return;
    }

    WriteTask processedTask = keyAttachment.getWriteTasks().peek();

    try {
      int writeCount = socketChannel.write(processedTask.getByteBuffer());
      if (writeCount < 0) {
        processedTask.getListener().onWriteFailed("Reached end of stream while writing", null);
        throw new EOFException("Reached end of stream while writing");
      }

      if (!processedTask.getByteBuffer().hasRemaining()) {
        processedTask.getListener().onWriteDone();
        keyAttachment.getWriteTasks().remove();
      }

    } catch (IOException e) {
      processedTask.getListener().onWriteFailed("I/O error occurs on write to channel " + socketChannel, new ConnectionClosedException(e));
      keyAttachment.getWriteTasks().clear();
      key.cancel();
    }
  }

  @Override
  public boolean accept(SelectionKey key) {
    return key.isValid() && key.isWritable();
  }
}

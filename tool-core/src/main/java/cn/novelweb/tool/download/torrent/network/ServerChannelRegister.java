package cn.novelweb.tool.download.torrent.network;


import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public interface ServerChannelRegister {

  /**
   * Create new channel and bind to specified selector
   *
   * @param selector specified selector
   * @return new created server channel
   */
  @NotNull
  ServerSocketChannel channelFor(Selector selector) throws IOException;

}

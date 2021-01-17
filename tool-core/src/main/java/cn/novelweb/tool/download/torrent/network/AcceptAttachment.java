package cn.novelweb.tool.download.torrent.network;

public interface AcceptAttachment {

  /**
   * @return channel listener factory for create listeners for new connections
   */
  ChannelListenerFactory getChannelListenerFactory();

}

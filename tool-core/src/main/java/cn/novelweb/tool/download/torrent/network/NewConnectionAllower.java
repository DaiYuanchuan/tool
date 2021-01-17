package cn.novelweb.tool.download.torrent.network;

public interface NewConnectionAllower {

  /**
   * @return true if we can accept new connection or can connect to other peer
   */
  boolean isNewConnectionAllowed();

}

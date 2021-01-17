package cn.novelweb.tool.download.torrent.client;


import com.sun.istack.internal.NotNull;

import java.io.IOException;

public interface TorrentLoader {

  /**
   * Creates or finds shared torrent instance for specified announceable torrent and return it
   *
   * @param loadedTorrent specified torrent
   * @return shared torrent instance associated with current announceable torrent
   * @throws IOException              if any io error occurs
   */
  @NotNull
  SharedTorrent loadTorrent(@NotNull LoadedTorrent loadedTorrent) throws IOException;

}

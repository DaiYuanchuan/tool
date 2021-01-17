package cn.novelweb.tool.download.torrent.client;


import cn.novelweb.tool.download.torrent.common.TorrentMetadata;
import com.sun.istack.internal.NotNull;

import java.io.IOException;

public interface TorrentMetadataProvider {

  /**
   * load and return new {@link TorrentMetadata} instance from any source
   *
   * @return new torrent metadata instance
   * @throws IOException               if any IO error occurs
   * @throws InvalidBEncodingException if specified source has invalid BEP format or missed required fields
   */
  @NotNull
  TorrentMetadata getTorrentMetadata() throws IOException;

}

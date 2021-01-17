package cn.novelweb.tool.download.torrent.client;

import cn.novelweb.tool.download.torrent.common.TorrentMetadata;
import cn.novelweb.tool.download.torrent.common.TorrentParser;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.IOException;

public class FileMetadataProvider implements TorrentMetadataProvider {

  private final String filePath;

  public FileMetadataProvider(String filePath) {
    this.filePath = filePath;
  }

  @NotNull
  @Override
  public TorrentMetadata getTorrentMetadata() throws IOException {
    File file = new File(filePath);
    return new TorrentParser().parseFromFile(file);
  }
}

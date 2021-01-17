package cn.novelweb.tool.download.torrent.client;

import cn.novelweb.tool.download.torrent.client.storage.PieceStorage;
import cn.novelweb.tool.download.torrent.common.*;
import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LoadedTorrentImpl implements LoadedTorrent {

  private final TorrentStatistic torrentStatistic;
  private final TorrentHash torrentHash;
  private final List<List<String>> announceUrls;
  private final String announce;
  private final PieceStorage pieceStorage;
  private final TorrentMetadataProvider metadataProvider;
  private final EventDispatcher eventDispatcher;

  LoadedTorrentImpl(TorrentStatistic torrentStatistic,
                    TorrentMetadataProvider metadataProvider,
                    TorrentMetadata torrentMetadata,
                    PieceStorage pieceStorage,
                    EventDispatcher eventDispatcher) {
    this.torrentStatistic = torrentStatistic;
    this.metadataProvider = metadataProvider;
    this.eventDispatcher = eventDispatcher;
    torrentHash = new ImmutableTorrentHash(torrentMetadata.getInfoHash());
    if (torrentMetadata.getAnnounceList() != null) {
      this.announceUrls = Collections.unmodifiableList(torrentMetadata.getAnnounceList());
    } else {
      this.announceUrls = Collections.singletonList(Collections.singletonList(torrentMetadata.getAnnounce()));
    }
    this.announce = torrentMetadata.getAnnounce();
    this.pieceStorage = pieceStorage;
  }

  @Override
  public PieceStorage getPieceStorage() {
    return pieceStorage;
  }

  @Override
  public TorrentMetadata getMetadata() {
    try {
      return metadataProvider.getTorrentMetadata();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to fetch torrent metadata from metadata provider: " + metadataProvider, e);
    }
  }

  @Override
  public TorrentStatistic getTorrentStatistic() {
    return torrentStatistic;
  }

  @Override
  @NotNull
  public AnnounceableInformation createAnnounceableInformation() {
    return new AnnounceableInformationImpl(
            torrentStatistic.getUploadedBytes(),
            torrentStatistic.getDownloadedBytes(),
            torrentStatistic.getLeftBytes(),
            torrentHash,
            announceUrls,
            announce
    );
  }

  @Override
  public TorrentHash getTorrentHash() {
    return torrentHash;
  }

  @Override
  public EventDispatcher getEventDispatcher() {
    return eventDispatcher;
  }

  @Override
  public String toString() {
    return "LoadedTorrentImpl{" +
            "piece storage='" + pieceStorage + '\'' +
            ", metadata provider='" + metadataProvider + '\'' +
            '}';
  }
}

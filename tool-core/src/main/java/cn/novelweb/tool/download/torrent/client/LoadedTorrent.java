package cn.novelweb.tool.download.torrent.client;


import cn.novelweb.tool.download.torrent.client.storage.PieceStorage;
import cn.novelweb.tool.download.torrent.common.AnnounceableInformation;
import cn.novelweb.tool.download.torrent.common.TorrentHash;
import cn.novelweb.tool.download.torrent.common.TorrentMetadata;
import cn.novelweb.tool.download.torrent.common.TorrentStatistic;
import com.sun.istack.internal.NotNull;

public interface LoadedTorrent {

  /**
   * @return {@link PieceStorage} where stored available pieces
   */
  PieceStorage getPieceStorage();

  /**
   * @return {@link TorrentMetadata} instance
   * @throws IllegalStateException if unable to fetch metadata from source
   *                               (e.g. source is .torrent file and it was deleted manually)
   */
  TorrentMetadata getMetadata() throws IllegalStateException;

  /**
   * @return new instance of {@link AnnounceableInformation} for announce this torrent to the tracker
   */
  @NotNull
  AnnounceableInformation createAnnounceableInformation();

  /**
   * @return {@link TorrentStatistic} instance related with this torrent
   */
  TorrentStatistic getTorrentStatistic();

  /**
   * @return hash of this torrent
   */
  TorrentHash getTorrentHash();

  /**
   * @return related {@link EventDispatcher}
   */
  EventDispatcher getEventDispatcher();

}

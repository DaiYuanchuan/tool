package cn.novelweb.tool.download.torrent.client.storage;

import cn.novelweb.tool.download.torrent.common.TorrentMetadata;

import java.util.BitSet;

public class FullyPieceStorageFactory implements PieceStorageFactory {

  public final static FullyPieceStorageFactory INSTANCE = new FullyPieceStorageFactory();

  private FullyPieceStorageFactory() {
  }

  @Override
  public PieceStorage createStorage(TorrentMetadata metadata, TorrentByteStorage byteStorage) {

    BitSet availablePieces = new BitSet(metadata.getPiecesCount());
    availablePieces.set(0, metadata.getPiecesCount());
    return new PieceStorageImpl(
            byteStorage,
            availablePieces,
            metadata.getPiecesCount(),
            metadata.getPieceLength()
    );
  }
}

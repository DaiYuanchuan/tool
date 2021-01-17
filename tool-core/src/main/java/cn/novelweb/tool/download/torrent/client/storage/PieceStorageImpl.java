package cn.novelweb.tool.download.torrent.client.storage;


import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PieceStorageImpl implements PieceStorage {

  private final TorrentByteStorage fileCollectionStorage;
  private final ReadWriteLock readWriteLock;

  private final Object openStorageLock = new Object();

  @Nullable
  private volatile BitSet availablePieces;
  private final int piecesCount;
  private final int pieceSize;
  private volatile boolean isOpen;
  private volatile boolean closedFully = false;

  public PieceStorageImpl(TorrentByteStorage fileCollectionStorage,
                          BitSet availablePieces,
                          int piecesCount,
                          int pieceSize) {
    this.fileCollectionStorage = fileCollectionStorage;
    this.readWriteLock = new ReentrantReadWriteLock();
    this.piecesCount = piecesCount;
    this.pieceSize = pieceSize;
    BitSet bitSet = new BitSet(piecesCount);
    bitSet.or(availablePieces);
    if (bitSet.cardinality() != piecesCount) {
      this.availablePieces = bitSet;
    }
    isOpen = false;
  }

  private void checkPieceIndex(int pieceIndex) {
    if (pieceIndex < 0 || pieceIndex >= piecesCount) {
      throw new IllegalArgumentException("Incorrect piece index " + pieceIndex + ". Piece index must be positive less than" + piecesCount);
    }
  }

  @Override
  public void savePiece(int pieceIndex, byte[] pieceData) throws IOException {
    checkPieceIndex(pieceIndex);
    try {
      readWriteLock.writeLock().lock();

      if (closedFully) throw new IOException("Storage is closed");

      BitSet availablePieces = this.availablePieces;

      boolean isFullyDownloaded = availablePieces == null;

      if (isFullyDownloaded) return;

      if (availablePieces.get(pieceIndex)) return;

      openStorageIsNecessary(false);

      long pos = pieceIndex;
      pos = pos * pieceSize;
      ByteBuffer buffer = ByteBuffer.wrap(pieceData);
      fileCollectionStorage.write(buffer, pos);

      availablePieces.set(pieceIndex);
      boolean isFullyNow = availablePieces.cardinality() == piecesCount;
      if (isFullyNow) {
        this.availablePieces = null;
        fileCollectionStorage.finish();
        fileCollectionStorage.close();
        fileCollectionStorage.open(true);
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  private void openStorageIsNecessary(boolean onlyRead) throws IOException {
    if (!isOpen) {
      fileCollectionStorage.open(onlyRead);
      isOpen = true;
    }
  }

  @Override
  public byte[] readPiecePart(int pieceIndex, int offset, int length) throws IOException {
    checkPieceIndex(pieceIndex);
    try {
      readWriteLock.readLock().lock();

      if (closedFully) throw new IOException("Storage is closed");

      BitSet availablePieces = this.availablePieces;
      if (availablePieces != null && !availablePieces.get(pieceIndex)) {
        throw new IllegalArgumentException("trying reading part of not available piece");
      }

      synchronized (openStorageLock) {
        openStorageIsNecessary(availablePieces == null);
      }

      ByteBuffer buffer = ByteBuffer.allocate(length);
      long pos = pieceIndex;
      pos = pos * pieceSize + offset;
      fileCollectionStorage.read(buffer, pos);
      return buffer.array();
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public boolean isFinished() {
    try {
      readWriteLock.readLock().lock();
      return availablePieces == null;
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public void closeFully() throws IOException {
    try {
      readWriteLock.writeLock().lock();
      close0();
      closedFully = true;
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public BitSet getAvailablePieces() {
    try {
      readWriteLock.readLock().lock();
      BitSet result = new BitSet(piecesCount);

      BitSet availablePieces = this.availablePieces;
      boolean isFullyDownloaded = availablePieces == null;

      if (isFullyDownloaded) {
        result.set(0, piecesCount);
        return result;
      }
      result.or(availablePieces);
      return result;
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public void close() throws IOException {
    try {
      readWriteLock.writeLock().lock();
      close0();
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  private void close0() throws IOException {
    if (!isOpen) return;
    fileCollectionStorage.close();
    isOpen = false;
  }
}

package cn.novelweb.tool.download.torrent.client;

import cn.novelweb.tool.download.torrent.common.TorrentHash;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class TorrentManagerImpl implements TorrentManager {

  private final EventDispatcher eventDispatcher;
  private final TorrentHash hash;

  TorrentManagerImpl(EventDispatcher eventDispatcher, TorrentHash hash) {
    this.eventDispatcher = eventDispatcher;
    this.hash = hash;
  }

  @Override
  public void addListener(TorrentListener listener) {
    eventDispatcher.addListener(listener);
  }

  @Override
  public boolean removeListener(TorrentListener listener) {
    return eventDispatcher.removeListener(listener);
  }

  @Override
  public byte[] getInfoHash() {
    return hash.getInfoHash();
  }

  @Override
  public String getHexInfoHash() {
    return hash.getHexInfoHash();
  }

  @Override
  public void awaitDownloadComplete(int timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
    final Semaphore semaphore = new Semaphore(0);
    TorrentListenerWrapper listener = new TorrentListenerWrapper() {
      @Override
      public void downloadComplete() {
        semaphore.release();
      }
    };
    try {
      addListener(listener);
      if (!semaphore.tryAcquire(timeout, timeUnit)) {
        throw new TimeoutException("Unable to download torrent in specified timeout");
      }
    } finally {
      removeListener(listener);
    }
  }
}

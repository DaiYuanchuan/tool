package cn.novelweb.tool.download.torrent.client;

import cn.novelweb.tool.download.torrent.common.AnnounceableInformation;
import cn.novelweb.tool.download.torrent.common.Pair;
import cn.novelweb.tool.download.torrent.common.TorrentUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TorrentsStorage {

  private final ReadWriteLock readWriteLock;
  private final Map<String, SharedTorrent> activeTorrents;
  private final Map<String, LoadedTorrent> loadedTorrents;

  public TorrentsStorage() {
    readWriteLock = new ReentrantReadWriteLock();
    activeTorrents = new HashMap<String, SharedTorrent>();
    loadedTorrents = new HashMap<String, LoadedTorrent>();
  }

  public boolean hasTorrent(String hash) {
    try {
      readWriteLock.readLock().lock();
      return loadedTorrents.containsKey(hash);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public LoadedTorrent getLoadedTorrent(String hash) {
    try {
      readWriteLock.readLock().lock();
      return loadedTorrents.get(hash);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public void peerDisconnected(String torrentHash) {
    final SharedTorrent torrent;
    try {
      readWriteLock.writeLock().lock();
      torrent = activeTorrents.get(torrentHash);
      if (torrent == null) return;

      boolean isTorrentFinished = torrent.isFinished();
      if (torrent.getDownloadersCount() == 0 && isTorrentFinished) {
        activeTorrents.remove(torrentHash);
      } else {
        return;
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
    torrent.close();
  }

  public SharedTorrent getTorrent(String hash) {
    try {
      readWriteLock.readLock().lock();
      return activeTorrents.get(hash);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public void addTorrent(String hash, LoadedTorrent torrent) {
    try {
      readWriteLock.writeLock().lock();
      loadedTorrents.put(hash, torrent);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  public SharedTorrent putIfAbsentActiveTorrent(String hash, SharedTorrent torrent) {
    try {
      readWriteLock.writeLock().lock();
      final SharedTorrent old = activeTorrents.get(hash);
      if (old != null) return old;

      return activeTorrents.put(hash, torrent);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  public Pair<SharedTorrent, LoadedTorrent> remove(String hash) {
    final Pair<SharedTorrent, LoadedTorrent> result;
    try {
      readWriteLock.writeLock().lock();
      final SharedTorrent sharedTorrent = activeTorrents.remove(hash);
      final LoadedTorrent loadedTorrent = loadedTorrents.remove(hash);
      result = new Pair<SharedTorrent, LoadedTorrent>(sharedTorrent, loadedTorrent);
    } finally {
      readWriteLock.writeLock().unlock();
    }
    if (result.second() != null) {
      try {
        result.second().getPieceStorage().close();
      } catch (IOException ignored) {
      }
    }
    if (result.first() != null) {
      result.first().closeFully();
    }
    return result;
  }

  public List<SharedTorrent> activeTorrents() {
    try {
      readWriteLock.readLock().lock();
      return new ArrayList<SharedTorrent>(activeTorrents.values());
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public List<AnnounceableInformation> announceableTorrents() {
    List<AnnounceableInformation> result = new ArrayList<AnnounceableInformation>();
    try {
      readWriteLock.readLock().lock();
      for (LoadedTorrent loadedTorrent : loadedTorrents.values()) {
        AnnounceableInformation announceableInformation = loadedTorrent.createAnnounceableInformation();
        if (TorrentUtils.isTrackerLessInfo(announceableInformation)) continue;
        result.add(announceableInformation);
      }
      return result;
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public List<LoadedTorrent> getLoadedTorrents() {
    try {
      readWriteLock.readLock().lock();
      return new ArrayList<LoadedTorrent>(loadedTorrents.values());
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public void clear() {
    final Collection<SharedTorrent> sharedTorrents;
    final Collection<LoadedTorrent> loadedTorrents;
    try {
      readWriteLock.writeLock().lock();
      sharedTorrents = new ArrayList<SharedTorrent>(activeTorrents.values());
      loadedTorrents = new ArrayList<LoadedTorrent>(this.loadedTorrents.values());
      this.loadedTorrents.clear();
      activeTorrents.clear();
    } finally {
      readWriteLock.writeLock().unlock();
    }
    for (SharedTorrent sharedTorrent : sharedTorrents) {
      sharedTorrent.closeFully();
    }
    for (LoadedTorrent loadedTorrent : loadedTorrents) {
      try {
        loadedTorrent.getPieceStorage().close();
      } catch (IOException ignored) {
      }
    }
  }
}

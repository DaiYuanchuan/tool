package cn.novelweb.tool.download.torrent.client.network;


import cn.novelweb.tool.download.torrent.Constants;
import cn.novelweb.tool.download.torrent.client.PeersStorage;
import cn.novelweb.tool.download.torrent.network.NewConnectionAllower;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * this implementation allows fixed count of open connection simultaneously
 */
public class CountLimitConnectionAllower implements NewConnectionAllower {

  private final PeersStorage myPeersStorage;

  private final AtomicInteger myMaxConnectionCount = new AtomicInteger();

  public CountLimitConnectionAllower(PeersStorage peersStorage) {
    this.myPeersStorage = peersStorage;
    myMaxConnectionCount.set(Constants.DEFAULT_MAX_CONNECTION_COUNT);

  }

  public void setMyMaxConnectionCount(int newMaxCount) {
    myMaxConnectionCount.set(newMaxCount);
  }

  @Override
  public boolean isNewConnectionAllowed() {
    return myPeersStorage.getSharingPeers().size() < myMaxConnectionCount.get();
  }
}

package cn.novelweb.tool.download.torrent.client.network;

import cn.novelweb.tool.download.torrent.client.Context;
import cn.novelweb.tool.download.torrent.common.TorrentHash;
import cn.novelweb.tool.download.torrent.network.ConnectionListener;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class OutgoingConnectionListener implements ConnectionListener {

  private volatile DataProcessor myNext;
  private final TorrentHash torrentHash;
  private final String myRemotePeerIp;
  private final int myRemotePeerPort;
  private final Context myContext;

  public OutgoingConnectionListener(Context context,
                                    TorrentHash torrentHash,
                                    String remotePeerIp,
                                    int remotePeerPort) {
    this.torrentHash = torrentHash;
    myRemotePeerIp = remotePeerIp;
    myRemotePeerPort = remotePeerPort;
    myNext = new ShutdownProcessor();
    myContext = context;
  }

  @Override
  public void onNewDataAvailable(SocketChannel socketChannel) throws IOException {
    this.myNext = this.myNext.processAndGetNext(socketChannel);
  }

  @Override
  public void onConnectionEstablished(SocketChannel socketChannel) throws IOException {
    HandshakeSender handshakeSender = new HandshakeSender(
            torrentHash,
            myRemotePeerIp,
            myRemotePeerPort,
            myContext);
    this.myNext = handshakeSender.processAndGetNext(socketChannel);
  }

  @Override
  public void onError(SocketChannel socketChannel, Throwable ex) throws IOException {
    this.myNext.handleError(socketChannel, ex);
  }
}

package cn.novelweb.tool.download.torrent.client;

public class TorrentListenerWrapper implements TorrentListener {

  @Override
  public void peerConnected(PeerInformation peerInformation) {

  }

  @Override
  public void peerDisconnected(PeerInformation peerInformation) {

  }

  @Override
  public void pieceDownloaded(PieceInformation pieceInformation, PeerInformation peerInformation) {

  }

  @Override
  public void downloadComplete() {

  }

  @Override
  public void downloadFailed(Throwable cause) {

  }

  @Override
  public void pieceReceived(PieceInformation pieceInformation, PeerInformation peerInformation) {

  }

  @Override
  public void validationComplete(int validpieces, int totalpieces) {

  }
}

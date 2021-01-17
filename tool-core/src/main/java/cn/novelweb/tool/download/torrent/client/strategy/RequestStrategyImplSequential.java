package cn.novelweb.tool.download.torrent.client.strategy;

import cn.novelweb.tool.download.torrent.client.Piece;

import java.util.BitSet;

/**
 * A sequential request strategy implementation.
 *
 * @author cjmalloy
 */
public class RequestStrategyImplSequential implements RequestStrategy {

  @Override
  public Piece choosePiece(BitSet interesting, Piece[] pieces) {

    for (Piece p : pieces) {
      if (interesting.get(p.getIndex())) return p;
    }
    return null;
  }
}

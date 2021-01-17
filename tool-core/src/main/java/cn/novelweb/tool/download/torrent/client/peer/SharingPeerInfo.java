package cn.novelweb.tool.download.torrent.client.peer;

import cn.novelweb.tool.download.torrent.common.TorrentHash;

import java.nio.ByteBuffer;

/**
 * @author Sergey.Pak
 * Date: 8/9/13
 * Time: 6:40 PM
 */
public interface SharingPeerInfo {

  String getIp();

  int getPort();

  TorrentHash getTorrentHash();

  ByteBuffer getPeerId();

}

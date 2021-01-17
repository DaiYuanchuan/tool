package cn.novelweb.tool.download.torrent.client;

public enum ClientState {
  WAITING,
  VALIDATING,
  SHARING,
  SEEDING,
  ERROR,
  DONE
}

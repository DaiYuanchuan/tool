package cn.novelweb.tool.download.torrent.tracker;

import cn.novelweb.tool.download.torrent.bcodec.BDecoder;
import cn.novelweb.tool.download.torrent.bcodec.BEValue;
import cn.novelweb.tool.download.torrent.bcodec.BEncoder;
import cn.novelweb.tool.download.torrent.common.TorrentLoggerFactory;
import cn.novelweb.tool.download.torrent.common.protocol.http.HTTPTrackerErrorMessage;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiAnnounceRequestProcessor {

  private final cn.novelweb.tool.download.torrent.tracker.TrackerRequestProcessor myTrackerRequestProcessor;

  private static final Logger logger =
          TorrentLoggerFactory.getLogger(MultiAnnounceRequestProcessor.class);

  public MultiAnnounceRequestProcessor(cn.novelweb.tool.download.torrent.tracker.TrackerRequestProcessor trackerRequestProcessor) {
    myTrackerRequestProcessor = trackerRequestProcessor;
  }

  public void process(final String body, final String url, final String hostAddress, final cn.novelweb.tool.download.torrent.tracker.TrackerRequestProcessor.RequestHandler requestHandler) throws IOException {

    final List<BEValue> responseMessages = new ArrayList<BEValue>();
    final AtomicBoolean isAnySuccess = new AtomicBoolean(false);
    for (String s : body.split("\n")) {
      myTrackerRequestProcessor.process(s, hostAddress, new cn.novelweb.tool.download.torrent.tracker.TrackerRequestProcessor.RequestHandler() {
        @Override
        public void serveResponse(int code, String description, ByteBuffer responseData) {
          isAnySuccess.set(isAnySuccess.get() || (code == HttpStatus.OK.value()));
          try {
            responseMessages.add(BDecoder.bdecode(responseData));
          } catch (IOException e) {
            logger.warn("cannot decode message from byte buffer");
          }
        }
      });
    }
    if (responseMessages.isEmpty()) {
      ByteBuffer res;
      HttpStatus status;
      res = HTTPTrackerErrorMessage.craft("").getData();
      status = HttpStatus.BAD_REQUEST;
      requestHandler.serveResponse(status.value(), "", res);
      return;
    }
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    BEncoder.bencode(responseMessages, out);
    requestHandler.serveResponse(isAnySuccess.get() ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value(), "", ByteBuffer.wrap(out.toByteArray()));
  }
}

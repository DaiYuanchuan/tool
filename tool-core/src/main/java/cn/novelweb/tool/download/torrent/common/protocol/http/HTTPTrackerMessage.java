/**
 * Copyright (C) 2012 Turn, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.novelweb.tool.download.torrent.common.protocol.http;

import cn.novelweb.tool.download.torrent.bcodec.BDecoder;
import cn.novelweb.tool.download.torrent.bcodec.BEValue;
import cn.novelweb.tool.download.torrent.common.protocol.TrackerMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;


/**
 * Base class for HTTP tracker messages.
 *
 * @author mpetazzoni
 */
public abstract class HTTPTrackerMessage extends TrackerMessage {

  protected HTTPTrackerMessage(Type type, ByteBuffer data) {
    super(type, data);
  }

  public static HTTPTrackerMessage parse(InputStream data)
          throws IOException, MessageValidationException {
    BEValue decoded = BDecoder.bdecode(data);
    if (decoded == null) {
      throw new MessageValidationException("Could not decode tracker message (not B-encoded?)!: ");
    }
    return parse(decoded);
  }

  public static HTTPTrackerMessage parse(BEValue decoded) throws IOException, MessageValidationException {
    Map<String, BEValue> params = decoded.getMap();

    if (params.containsKey("info_hash")) {
      return HTTPAnnounceRequestMessage.parse(decoded);
    } else if (params.containsKey("peers")) {
      return HTTPAnnounceResponseMessage.parse(decoded);
    } else if (params.containsKey("failure reason")) {
      return HTTPTrackerErrorMessage.parse(decoded);
    }

    throw new MessageValidationException("Unknown HTTP tracker message!");
  }

}

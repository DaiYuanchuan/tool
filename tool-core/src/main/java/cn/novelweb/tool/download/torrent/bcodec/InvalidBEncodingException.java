package cn.novelweb.tool.download.torrent.bcodec;

import java.io.IOException;

/**
 * Exception thrown when a B-encoded stream cannot be decoded.
 *
 * @author mpetazzoni
 */
public class InvalidBEncodingException extends IOException {

    public static final long serialVersionUID = -1;

    public InvalidBEncodingException(String message) {
        super(message);
    }
}

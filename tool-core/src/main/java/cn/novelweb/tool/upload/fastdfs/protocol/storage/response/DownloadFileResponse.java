package cn.novelweb.tool.upload.fastdfs.protocol.storage.response;

import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.callback.DownloadCallback;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * <p></p>
 * <p>2020-02-03 17:00</p>
 *
 * @author LiZW
 **/
public class DownloadFileResponse<T> extends BaseResponse<T> {

    private DownloadCallback<T> callback;

    public DownloadFileResponse(DownloadCallback<T> callback) {
        super();
        this.callback = callback;
    }

    /**
     * 解析反馈内容
     */
    @Override
    public T decodeContent(InputStream in, Charset charset) throws IOException {
        // 解析报文内容
        FastDfsInputStream input = new FastDfsInputStream(in, getContentLength());
        return callback.receive(input);
    }

}

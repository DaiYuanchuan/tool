package cn.novelweb.tool.upload.fastdfs.protocol.storage.callback;

import cn.novelweb.tool.upload.fastdfs.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>直接返回Byte[]数据</p>
 * <p>2020-02-03 16:47</p>
 *
 * @author LiZW
 **/
public class DownloadByteArray implements DownloadCallback<byte[]> {
    @Override
    public byte[] receive(InputStream inputStream) throws IOException {
        return IoUtils.toByteArray(inputStream);
    }
}

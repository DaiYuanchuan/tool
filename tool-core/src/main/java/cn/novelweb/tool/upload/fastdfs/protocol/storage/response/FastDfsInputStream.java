package cn.novelweb.tool.upload.fastdfs.protocol.storage.response;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>封装FastDFS数据流</p>
 * <p>2020-02-03 17:01</p>
 *
 * @author Dai Yuanchuan
 **/
public class FastDfsInputStream extends InputStream {

    private final InputStream inputStream;
    private final long size;
    private long remainByteSize;

    public FastDfsInputStream(InputStream ins, long size) {
        this.inputStream = ins;
        this.size = size;
        remainByteSize = size;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] args, int off, int len) throws IOException {
        if (remainByteSize == 0) {
            return -1;
        }
        int byteSize = inputStream.read(args, off, len);
        if (remainByteSize < byteSize) {
            throw new IOException("协议长度" + size + "与实际长度不符");
        }
        remainByteSize -= byteSize;
        return byteSize;
    }

    @Override
    public void close() throws IOException {
    }

    /**
     * 是否已完成读取
     */
    public boolean isReadCompleted() {
        return remainByteSize == 0;
    }

}

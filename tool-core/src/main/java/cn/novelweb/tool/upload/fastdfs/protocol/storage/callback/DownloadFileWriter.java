package cn.novelweb.tool.upload.fastdfs.protocol.storage.callback;

import cn.novelweb.tool.upload.fastdfs.utils.IoUtils;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>直接把文件下载到本地文件系统</p>
 * <p>2020-02-03 16:50</p>
 *
 * @author LiZW
 **/
public class DownloadFileWriter implements DownloadCallback<String> {

    /**
     * 文件名称
     */
    private String fileName;

    public DownloadFileWriter(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 文件下载处理
     *
     * @return 返回文件名称
     */
    @Override
    public String receive(InputStream inputStream) throws IOException {
        FileOutputStream out = null;
        InputStream in = null;
        try {
            out = new FileOutputStream(fileName);
            in = new BufferedInputStream(inputStream);
            // 通过ioUtil 对接输入输出流，实现文件下载
            IoUtils.copy(in, out);
            out.flush();
        } finally {
            // 关闭流
            IoUtils.closeQuietly(in);
            IoUtils.closeQuietly(out);
        }
        return fileName;
    }

}

package cn.novelweb.tool.upload.fastdfs.conn;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * <p>表示一个客户端与服务端的连接，负责连接的管理</p>
 * <p>2020-02-03 16:39</p>
 *
 * @author LiZW
 **/
public interface Connection extends Closeable {

    /**
     * 关闭连接
     */
    @Override
    void close();

    /**
     * 连接是否关闭
     *
     * @return 返回true表示连接关闭
     */
    boolean isClosed();

    /**
     * 测试连接是否有效
     *
     * @return 返回true表示连接无效
     */
    boolean isValid();

    /**
     * 获取输出流
     *
     * @return 输出流
     * @throws IOException 操作异常
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * 获取输入流
     *
     * @return 输入流
     * @throws IOException 获取输入流错误
     */
    InputStream getInputStream() throws IOException;

    /**
     * 获取字符集
     *
     * @return 字符集
     */
    Charset getCharset();

}

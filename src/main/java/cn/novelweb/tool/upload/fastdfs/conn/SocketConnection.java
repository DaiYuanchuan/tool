package cn.novelweb.tool.upload.fastdfs.conn;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.exception.FastDfsConnectException;
import cn.novelweb.tool.upload.fastdfs.utils.BytesUtil;
import cn.novelweb.tool.upload.fastdfs.utils.IoUtils;
import cn.novelweb.tool.upload.fastdfs.utils.Log;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * <p>默认连接实现</p>
 * <p>2020-02-03 16:40</p>
 *
 * @author LiZW
 **/
@Slf4j
public class SocketConnection implements Connection {

    /**
     * 封装socket
     */
    private Socket socket;

    /**
     * 字符集
     */
    private Charset charset;

    /**
     * 创建与服务端连接
     *
     * @param address        连接地址
     * @param soTimeout      soTimeout
     * @param connectTimeout 设置连接超时
     */
    public SocketConnection(InetSocketAddress address, int soTimeout, int connectTimeout, Charset charset) {
        try {
            socket = new Socket();
            socket.setSoTimeout(soTimeout);
            Log.debug("开始连接到服务器 {} soTimeout={} connectTimeout={}", address, soTimeout, connectTimeout);
            this.charset = charset;
            socket.connect(address, connectTimeout);
            Log.debug("成功连接到服务器:{}", address);
        } catch (IOException e) {
            throw new FastDfsConnectException("不能连接到服务器:" + address, e);
        }
    }

    /**
     * 正常关闭连接
     */
    @Override
    public synchronized void close() {
        Log.debug("断开连接, 服务器地址:{}", socket);
        byte[] header = new byte[OtherConstants.DFS_PROTO_PKG_LEN_SIZE + 2];
        Arrays.fill(header, (byte) 0);
        byte[] hexLen = BytesUtil.long2buff(0);
        System.arraycopy(hexLen, 0, header, 0, hexLen.length);
        header[OtherConstants.PROTO_HEADER_CMD_INDEX] = CmdConstants.DFS_PROTO_CMD_QUIT;
        header[OtherConstants.PROTO_HEADER_STATUS_INDEX] = (byte) 0;
        try {
            socket.getOutputStream().write(header);
            socket.close();
        } catch (IOException e) {
            log.error("关闭连接失败", e);
        } finally {
            IoUtils.closeQuietly(socket);
        }
    }

    /**
     * 连接是否关闭
     */
    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    /**
     * 检查连接是否有效
     */
    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean isValid() {
        Log.debug("检查连接状态 {} ", this.socket);
        try {
            byte[] header = new byte[OtherConstants.DFS_PROTO_PKG_LEN_SIZE + 2];
            Arrays.fill(header, (byte) 0);

            byte[] hexLen = BytesUtil.long2buff(0);
            System.arraycopy(hexLen, 0, header, 0, hexLen.length);
            header[OtherConstants.PROTO_HEADER_CMD_INDEX] = CmdConstants.DFS_PROTO_CMD_ACTIVE_TEST;
            header[OtherConstants.PROTO_HEADER_STATUS_INDEX] = (byte) 0;
            socket.getOutputStream().write(header);
            if (socket.getInputStream().read(header) != header.length) {
                return false;
            }
            return header[OtherConstants.PROTO_HEADER_STATUS_INDEX] == 0;
        } catch (IOException e) {
            log.error("检查连接状态异常", e);
            return false;
        }
    }

    /**
     * 获取输出流
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    /**
     * 获取输入流
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    /**
     * 获取字符集
     */
    @Override
    public Charset getCharset() {
        return charset;
    }

}

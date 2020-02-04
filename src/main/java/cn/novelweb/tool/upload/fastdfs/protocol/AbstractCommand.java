package cn.novelweb.tool.upload.fastdfs.protocol;

import cn.novelweb.tool.upload.fastdfs.conn.Connection;
import cn.novelweb.tool.upload.fastdfs.exception.FastDfsIoException;
import cn.novelweb.tool.upload.fastdfs.utils.Log;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * <p>FastDFS命令操执行抽象类</p>
 * <p>2020-02-03 16:44</p>
 *
 * @author LiZW
 **/
@Slf4j
public abstract class AbstractCommand<T> implements BaseCommand<T> {

    /**
     * 请求对象
     */
    public BaseRequest request;

    /**
     * 响应对象
     */
    public BaseResponse<T> response;

    /**
     * 对服务端发出请求然后接收反馈
     */
    @Override
    public T execute(Connection conn) {
        // 封装socket交易 send
        try {
            send(conn.getOutputStream(), conn.getCharset());
        } catch (IOException e) {
            throw new FastDfsIoException("Socket IO异常 发送消息异常", e);
        }
        try {
            return receive(conn.getInputStream(), conn.getCharset());
        } catch (IOException e) {
            throw new FastDfsIoException("Socket IO异常 接收消息异常", e);
        }
    }

    /**
     * 将报文输出规范为模板方法<br/>
     * 1.输出报文头<br/>
     * 2.输出报文参数<br/>
     * 3.输出文件内容<br/>
     */
    private void send(OutputStream out, Charset charset) throws IOException {
        // 报文分为三个部分
        // 报文头
        byte[] head = request.getHeadByte(charset);
        // 请求参数
        byte[] param = request.encodeParam(charset);
        // 交易文件流
        InputStream inputFile = request.getInputFile();
        long fileSize = request.getFileSize();
        Log.debug("发出请求 - 报文头[{}], 请求参数[{}]", request.getHead(), param);
        // 输出报文头
        out.write(head);
        // 输出交易参数
        if (null != param) {
            out.write(param);
        }
        // 输出文件流
        if (null != inputFile) {
            sendFileContent(inputFile, fileSize, out);
        }
    }

    /**
     * 发送文件
     */
    private void sendFileContent(InputStream ins, long size, OutputStream ous) throws IOException {
        Log.debug("开始上传文件流, 大小为[{}]", size);
        long remainBytes = size;
        byte[] buff = new byte[256 * 1024];
        int bytes;
        while (remainBytes > 0) {
            if ((bytes = ins.read(buff, 0, remainBytes > buff.length ? buff.length : (int) remainBytes)) < 0) {
                throw new IOException("数据流已结束, 不匹配预期的大小");
            }
            ous.write(buff, 0, bytes);
            remainBytes -= bytes;
            Log.debug("剩余上传数据量[{}]", remainBytes);
        }
    }

    /**
     * 接收相应数据,这里只能解析报文头
     * 报文内容(参数+文件)只能靠接收对象(对应的Response对象)解析
     */
    private T receive(InputStream in, Charset charset) throws IOException {
        // 解析报文头
        ProtocolHead head = ProtocolHead.createFromInputStream(in);
        Log.debug("服务端返回报文头{}", head);
        // 校验报文头
        head.validateResponseHead();
        // 解析报文体
        return response.decode(head, in, charset);
    }
}

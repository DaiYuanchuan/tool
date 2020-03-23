package cn.novelweb.tool.upload.fastdfs.exception;

/**
 * <p>非FastDFS本身的错误码抛出的异常，而是java客户端向服务端发送命令、文件或从服务端读取结果、下载文件时发生io异常</p>
 * <p>2020-02-03 16:19</p>
 *
 * @author LiZW
 **/
public class FastDfsIoException extends FastDfsException {
    public FastDfsIoException(Throwable cause) {
        super("客户端连接服务端出现了io异常", cause);
    }
    public FastDfsIoException(String message, Throwable cause) {
        super("客户端连接服务端出现了io异常:" + message, cause);
    }
    public FastDfsIoException(String message) {
        super(message);
    }
}

package cn.novelweb.tool.upload.fastdfs.exception;

/**
 * <p>FastDFS客户端异常 基类</p>
 * <p>2020-02-03 16:18</p>
 *
 * @author LiZW
 **/
public class FastDfsException extends RuntimeException {
    public FastDfsException(String message) {
        super(message);
    }
    public FastDfsException(String message, Throwable cause) {
        super(message, cause);
    }
}

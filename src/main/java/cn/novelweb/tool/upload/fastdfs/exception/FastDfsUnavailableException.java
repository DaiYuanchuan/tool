package cn.novelweb.tool.upload.fastdfs.exception;

/**
 * <p>非FastDFS本身的错误码抛出的异常，取服务端连接取不到时抛出的异常</p>
 * <p>2020-02-03 16:22</p>
 *
 * @author LiZW
 **/
public class FastDfsUnavailableException extends FastDfsException {
    public FastDfsUnavailableException(String message) {
        super("无法获取服务端连接资源：" + message);
    }
    public FastDfsUnavailableException(String message, Throwable t) {
        super("无法获取服务端连接资源：" + message, t);
    }
}

package cn.novelweb.tool.upload.fastdfs.exception;

/**
 * <p>非FastDFS本身的错误码抛出的异常，socket连不上时抛出的异常</p>
 * <p>2020-02-03 16:18</p>
 *
 * @author LiZW
 **/
public class FastDfsConnectException extends FastDfsUnavailableException {
    public FastDfsConnectException(String message, Throwable t) {
        super(message, t);
    }

}

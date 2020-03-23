package cn.novelweb.tool.upload.fastdfs.exception;

/**
 * <p>上传图片异常</p>
 * <p>2020-02-03 16:24</p>
 *
 * @author Dai Yuanchuan
 **/
public class FastDfsUploadImageException extends FastDfsException {
    protected FastDfsUploadImageException(String message) {
        super(message);
    }

    public FastDfsUploadImageException(String message, Throwable cause) {
        super(message, cause);
    }
}

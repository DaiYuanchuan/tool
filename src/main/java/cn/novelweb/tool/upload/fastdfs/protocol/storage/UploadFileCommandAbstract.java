package cn.novelweb.tool.upload.fastdfs.protocol.storage;

import cn.novelweb.tool.upload.fastdfs.model.StorePath;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.request.UploadFileRequest;

import java.io.InputStream;

/**
 * <p>文件上传命令</p>
 * <p>2020-02-03 17:09</p>
 *
 * @author Dai Yuanchuan
 **/
public class UploadFileCommandAbstract extends AbstractStorageCommand<StorePath> {

    /**
     * 文件上传命令
     *
     * @param storeIndex     存储节点
     * @param inputStream    输入流
     * @param fileExtName    文件扩展名
     * @param fileSize       文件大小
     * @param isAppenderFile 是否支持断点续传
     */
    public UploadFileCommandAbstract(byte storeIndex, InputStream inputStream, String fileExtName, long fileSize, boolean isAppenderFile) {
        super();
        this.request = new UploadFileRequest(storeIndex, inputStream, fileExtName, fileSize, isAppenderFile);
        // 输出响应
        this.response = new BaseResponse<StorePath>() {
        };
    }
}

package cn.novelweb.tool.upload.fastdfs.protocol.storage;

import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.request.AppendFileRequest;

import java.io.InputStream;

/**
 * <p>添加文件命令</p>
 * <p>2020-02-03 17:02</p>
 *
 * @author Dai Yuanchuan
 **/
public class AppendFileCommandAbstract extends AbstractStorageCommand<Void> {

    public AppendFileCommandAbstract(InputStream inputStream, long fileSize, String path) {
        this.request = new AppendFileRequest(inputStream, fileSize, path);
        // 输出响应
        this.response = new BaseResponse<Void>() {
        };
    }

}

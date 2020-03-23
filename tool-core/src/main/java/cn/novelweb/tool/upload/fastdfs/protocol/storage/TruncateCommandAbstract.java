package cn.novelweb.tool.upload.fastdfs.protocol.storage;

import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.request.TruncateRequest;

/**
 * <p>文件Truncate命令</p>
 * <p>2020-02-03 17:08</p>
 *
 * @author Dai Yuanchuan
 **/
public class TruncateCommandAbstract extends AbstractStorageCommand<Void> {

    /**
     * 文件Truncate命令
     *
     * @param path     文件路径
     * @param fileSize 文件大小
     */
    public TruncateCommandAbstract(String path, long fileSize) {
        super();
        this.request = new TruncateRequest(path, fileSize);
        // 输出响应
        this.response = new BaseResponse<Void>() {
        };
    }
}

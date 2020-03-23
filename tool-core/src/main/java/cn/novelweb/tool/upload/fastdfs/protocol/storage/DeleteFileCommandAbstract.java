package cn.novelweb.tool.upload.fastdfs.protocol.storage;

import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.request.DeleteFileRequest;

/**
 * <p>删除文件爱你命令</p>
 * <p>2020-02-03 17:05</p>
 *
 * @author LiZW
 **/
public class DeleteFileCommandAbstract extends AbstractStorageCommand<Void> {

    /**
     * 文件删除命令
     *
     * @param groupName 组名
     * @param path      文件路径
     */
    public DeleteFileCommandAbstract(String groupName, String path) {
        super();
        this.request = new DeleteFileRequest(groupName, path);
        // 输出响应
        this.response = new BaseResponse<Void>() {
        };
    }

}

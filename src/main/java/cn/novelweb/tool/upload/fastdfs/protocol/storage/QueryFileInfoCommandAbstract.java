package cn.novelweb.tool.upload.fastdfs.protocol.storage;

import cn.novelweb.tool.upload.fastdfs.model.FileInfo;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.request.QueryFileInfoRequest;

/**
 * <p></p>
 * <p>2020-02-03 17:07</p>
 *
 * @author LiZW
 **/
public class QueryFileInfoCommandAbstract extends AbstractStorageCommand<FileInfo> {

    /**
     * 文件上传命令
     *
     * @param groupName 组名称
     * @param path      文件路径
     */
    public QueryFileInfoCommandAbstract(String groupName, String path) {
        super();
        this.request = new QueryFileInfoRequest(groupName, path);
        // 输出响应
        this.response = new BaseResponse<FileInfo>() {
        };
    }

}

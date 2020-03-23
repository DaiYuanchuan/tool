package cn.novelweb.tool.upload.fastdfs.protocol.tracker;

import cn.novelweb.tool.upload.fastdfs.model.StorageNodeInfo;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.tracker.request.GetFetchStorageRequest;

/**
 * <p>获取文件源存储服务器</p>
 * <p>2020-02-03 17:16</p>
 *
 * @author Dai Yuanchuan
 **/
public class GetFetchStorageCommandAbstract extends AbstractTrackerCommand<StorageNodeInfo> {

    /**
     * 获取文件源服务器
     *
     * @param groupName 组名称
     * @param path      路径
     * @param toUpdate  toUpdate
     */
    public GetFetchStorageCommandAbstract(String groupName, String path, boolean toUpdate) {
        super.request = new GetFetchStorageRequest(groupName, path, toUpdate);
        super.response = new BaseResponse<StorageNodeInfo>() {
        };
    }
}

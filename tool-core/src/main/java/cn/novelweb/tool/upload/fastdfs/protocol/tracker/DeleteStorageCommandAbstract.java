package cn.novelweb.tool.upload.fastdfs.protocol.tracker;

import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.tracker.request.DeleteStorageRequest;

/**
 * <p>删除存储服务器</p>
 * <p>2020-02-03 17:16</p>
 *
 * @author LiZW
 **/
public class DeleteStorageCommandAbstract extends AbstractTrackerCommand<Void> {

    public DeleteStorageCommandAbstract(String groupName, String storageIpAddr) {
        super.request = new DeleteStorageRequest(groupName, storageIpAddr);
        super.response = new BaseResponse<Void>() {
        };
    }
}

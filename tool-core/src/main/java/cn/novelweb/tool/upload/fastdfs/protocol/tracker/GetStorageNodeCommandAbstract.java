package cn.novelweb.tool.upload.fastdfs.protocol.tracker;

import cn.novelweb.tool.upload.fastdfs.model.StorageNode;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.tracker.request.GetStorageNodeByGroupNameRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.tracker.request.GetStorageNodeRequest;

/**
 * <p>获取存储节点命令</p>
 * <p>2020-02-03 17:17</p>
 *
 * @author Dai Yuanchuan
 **/
public class GetStorageNodeCommandAbstract extends AbstractTrackerCommand<StorageNode> {

    public GetStorageNodeCommandAbstract(String groupName) {
        super.request = new GetStorageNodeByGroupNameRequest(groupName);
        super.response = new BaseResponse<StorageNode>() {
        };
    }

    public GetStorageNodeCommandAbstract() {
        super.request = new GetStorageNodeRequest();
        super.response = new BaseResponse<StorageNode>() {
        };
    }
}

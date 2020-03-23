package cn.novelweb.tool.upload.fastdfs.protocol.tracker;

import cn.novelweb.tool.upload.fastdfs.model.StorageState;
import cn.novelweb.tool.upload.fastdfs.protocol.tracker.request.GetListStorageRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.tracker.response.GetListStorageResponse;

import java.util.List;

/**
 * <p>获取Storage服务器状态命令</p>
 * <p>2020-02-03 17:17</p>
 *
 * @author LiZW
 **/
public class GetStorageListCommandAbstract extends AbstractTrackerCommand<List<StorageState>> {

    public GetStorageListCommandAbstract(String groupName, String storageIpAddr) {
        super.request = new GetListStorageRequest(groupName, storageIpAddr);
        super.response = new GetListStorageResponse();
    }

    public GetStorageListCommandAbstract(String groupName) {
        super.request = new GetListStorageRequest(groupName);
        super.response = new GetListStorageResponse();
    }
}

package cn.novelweb.tool.upload.fastdfs.protocol.tracker;

import cn.novelweb.tool.upload.fastdfs.model.GroupState;
import cn.novelweb.tool.upload.fastdfs.protocol.tracker.request.GetGroupListRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.tracker.response.GetGroupListResponse;

import java.util.List;

/**
 * <p>获取Group信息命令</p>
 * <p>2020-02-03 17:17</p>
 *
 * @author Dai Yuanchuan
 **/
public class GetGroupListCommandAbstract extends AbstractTrackerCommand<List<GroupState>> {

    public GetGroupListCommandAbstract() {
        super.request = new GetGroupListRequest();
        super.response = new GetGroupListResponse();
    }
}

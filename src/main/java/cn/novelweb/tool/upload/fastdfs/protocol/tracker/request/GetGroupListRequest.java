package cn.novelweb.tool.upload.fastdfs.protocol.tracker.request;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.ProtocolHead;

/**
 * <p>获取Group信息请求</p>
 * <p>2020-02-03 17:12</p>
 *
 * @author LiZW
 **/
public class GetGroupListRequest extends BaseRequest {
    public GetGroupListRequest() {
        head = new ProtocolHead(CmdConstants.TRACKER_PROTO_CMD_SERVER_LIST_GROUP);
    }
}

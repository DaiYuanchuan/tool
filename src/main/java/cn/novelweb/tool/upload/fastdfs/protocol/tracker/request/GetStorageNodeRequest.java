package cn.novelweb.tool.upload.fastdfs.protocol.tracker.request;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.ProtocolHead;

/**
 * <p>获取存储节点请求</p>
 * <p>2020-02-03 17:14</p>
 *
 * @author LiZW
 **/
public class GetStorageNodeRequest extends BaseRequest {

    /**
     * 获取存储节点
     */
    public GetStorageNodeRequest() {
        super();
        this.head = new ProtocolHead(CmdConstants.TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE);
    }

}

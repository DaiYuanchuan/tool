package cn.novelweb.tool.upload.fastdfs.protocol.tracker.request;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.ProtocolHead;
import cn.novelweb.tool.upload.fastdfs.utils.Validate;

/**
 * <p>按分组获取存储节点请求(根据Group Name)</p>
 * <p>2020-02-03 17:13</p>
 *
 * @author Dai Yuanchuan
 **/
public class GetStorageNodeByGroupNameRequest extends BaseRequest {

    /**
     * 分组定义
     */
    @FastDfsColumn(index = 0, max = OtherConstants.DFS_GROUP_NAME_MAX_LEN)
    private final String groupName;

    /**
     * 获取存储节点
     *
     * @param groupName 分组名称
     */
    public GetStorageNodeByGroupNameRequest(String groupName) {
        Validate.notBlank(groupName, "分组不能为空");
        this.groupName = groupName;
        this.head = new ProtocolHead(CmdConstants.TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ONE);
    }

    public String getGroupName() {
        return groupName;
    }
}

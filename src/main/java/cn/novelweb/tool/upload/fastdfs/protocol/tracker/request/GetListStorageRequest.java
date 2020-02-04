package cn.novelweb.tool.upload.fastdfs.protocol.tracker.request;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.DynamicFieldType;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.ProtocolHead;
import cn.novelweb.tool.upload.fastdfs.utils.Validate;

/**
 * <p>获取Storage服务器状态请求</p>
 * <p>2020-02-03 17:12</p>
 *
 * @author Dai Yuanchuan
 **/
public class GetListStorageRequest extends BaseRequest {

    /**
     * 组名
     */
    @FastDfsColumn(index = 0, max = OtherConstants.DFS_GROUP_NAME_MAX_LEN)
    private String groupName;

    /**
     * 存储服务器ip地址
     */
    @FastDfsColumn(index = 1, max = OtherConstants.DFS_IP_ADDR_SIZE - 1, dynamicField = DynamicFieldType.nullable)
    private String storageIpAddr;

    private GetListStorageRequest() {
        head = new ProtocolHead(CmdConstants.TRACKER_PROTO_CMD_SERVER_LIST_STORAGE);
    }

    /**
     * 列举存储服务器状态
     */
    public GetListStorageRequest(String groupName, String storageIpAddr) {
        this();
        Validate.notBlank(groupName, "分组不能为空");
        this.groupName = groupName;
        this.storageIpAddr = storageIpAddr;
    }

    /**
     * 列举组当中存储节点状态
     */
    public GetListStorageRequest(String groupName) {
        this();
        this.groupName = groupName;
        Validate.notBlank(groupName, "分组不能为空");
    }

    public String getGroupName() {
        return groupName;
    }

    public String getStorageIpAddr() {
        return storageIpAddr;
    }

}

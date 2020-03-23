package cn.novelweb.tool.upload.fastdfs.protocol.tracker.request;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.ProtocolHead;
import cn.novelweb.tool.upload.fastdfs.utils.Validate;

/**
 * <p>删除存储服务器 请求</p>
 * <p>2020-02-03 17:10</p>
 *
 * @author Dai Yuanchuan
 **/
public class DeleteStorageRequest extends BaseRequest {

    /**
     * 组名
     */
    @FastDfsColumn(index = 0, max = OtherConstants.DFS_GROUP_NAME_MAX_LEN)
    private String groupName;

    /**
     * 存储ip
     */
    @FastDfsColumn(index = 1, max = OtherConstants.DFS_IP_ADDR_SIZE - 1)
    private String storageIpAddr;

    /**
     * 获取文件源服务器
     *
     * @param groupName     组名称
     * @param storageIpAddr 存储服务器IP地址
     */
    public DeleteStorageRequest(String groupName, String storageIpAddr) {
        Validate.notBlank(groupName, "分组不能为空");
        Validate.notBlank(storageIpAddr, "文件路径不能为空");
        this.groupName = groupName;
        this.storageIpAddr = storageIpAddr;
        head = new ProtocolHead(CmdConstants.TRACKER_PROTO_CMD_SERVER_DELETE_STORAGE);
    }

    public String getGroupName() {
        return groupName;
    }

    public String getStorageIpAddr() {
        return storageIpAddr;
    }

}

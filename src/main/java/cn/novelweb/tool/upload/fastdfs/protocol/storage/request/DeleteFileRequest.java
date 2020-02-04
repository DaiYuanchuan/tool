package cn.novelweb.tool.upload.fastdfs.protocol.storage.request;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.DynamicFieldType;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.ProtocolHead;

/**
 * <p></p>
 * <p>2020-02-03 16:52</p>
 *
 * @author LiZW
 **/
public class DeleteFileRequest extends BaseRequest {

    /**
     * 组名
     */
    @FastDfsColumn(index = 0, max = OtherConstants.DFS_GROUP_NAME_MAX_LEN)
    private String groupName;

    /**
     * 路径名
     */
    @FastDfsColumn(index = 1, dynamicField = DynamicFieldType.allRestByte)
    private String path;

    /**
     * 删除文件命令
     *
     * @param groupName 组名
     * @param path      文件路径
     */
    public DeleteFileRequest(String groupName, String path) {
        super();
        this.groupName = groupName;
        this.path = path;
        this.head = new ProtocolHead(CmdConstants.STORAGE_PROTO_CMD_DELETE_FILE);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}

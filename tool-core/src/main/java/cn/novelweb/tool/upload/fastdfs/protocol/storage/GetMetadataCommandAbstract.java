package cn.novelweb.tool.upload.fastdfs.protocol.storage;

import cn.novelweb.tool.upload.fastdfs.model.MateData;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.request.GetMetadataRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.response.GetMetadataResponse;

import java.util.Set;

/**
 * <p></p>
 * <p>2020-02-03 17:06</p>
 *
 * @author LiZW
 **/
public class GetMetadataCommandAbstract extends AbstractStorageCommand<Set<MateData>> {

    /**
     * 设置文件标签(元数据)
     *
     * @param groupName 组名
     * @param path      文件路径
     */
    public GetMetadataCommandAbstract(String groupName, String path) {
        this.request = new GetMetadataRequest(groupName, path);
        // 输出响应
        this.response = new GetMetadataResponse();
    }

}

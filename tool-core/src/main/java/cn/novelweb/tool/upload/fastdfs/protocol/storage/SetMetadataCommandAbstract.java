package cn.novelweb.tool.upload.fastdfs.protocol.storage;

import cn.novelweb.tool.upload.fastdfs.model.MateData;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.enums.StorageMetadataSetType;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.request.SetMetadataRequest;

import java.util.Set;

/**
 * <p>设置文件标签(文件元数据)</p>
 * <p>2020-02-03 17:08</p>
 *
 * @author LiZW
 **/
public class SetMetadataCommandAbstract extends AbstractStorageCommand<Void> {

    /**
     * 设置文件标签(元数据)
     *
     * @param groupName   组名称
     * @param path        路径
     * @param metaDataSet 元数据集合
     * @param type        增加元数据的类型
     */
    public SetMetadataCommandAbstract(String groupName, String path, Set<MateData> metaDataSet, StorageMetadataSetType type) {
        this.request = new SetMetadataRequest(groupName, path, metaDataSet, type);
        // 输出响应
        this.response = new BaseResponse<Void>() {
        };
    }

}

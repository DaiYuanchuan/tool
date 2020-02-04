package cn.novelweb.tool.upload.fastdfs.protocol.storage;

import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.request.ModifyRequest;

import java.io.InputStream;

/**
 * <p>文件修改命令</p>
 * <p>2020-02-03 17:07</p>
 *
 * @author Dai Yuanchuan
 **/
public class ModifyCommandAbstract extends AbstractStorageCommand<Void> {

    /**
     * 文件修改命令
     *
     * @param path        文件路径
     * @param inputStream 输入流
     * @param fileSize    文件大小
     * @param fileOffset  开始位置
     */
    public ModifyCommandAbstract(String path, InputStream inputStream, long fileSize, long fileOffset) {
        super();
        this.request = new ModifyRequest(inputStream, fileSize, path, fileOffset);
        // 输出响应
        this.response = new BaseResponse<Void>() {
        };
    }
}

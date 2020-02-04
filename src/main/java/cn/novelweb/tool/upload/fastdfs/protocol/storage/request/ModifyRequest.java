package cn.novelweb.tool.upload.fastdfs.protocol.storage.request;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.DynamicFieldType;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.ProtocolHead;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * <p></p>
 * <p>2020-02-03 16:54</p>
 *
 * @author LiZW
 **/
public class ModifyRequest extends BaseRequest {

    /**
     * 文件路径长度
     */
    @FastDfsColumn(index = 0)
    private long pathSize;

    /**
     * 开始位置
     */
    @FastDfsColumn(index = 1)
    private long fileOffset;

    /**
     * 发送文件长度
     */
    @FastDfsColumn(index = 2)
    private long fileSize;

    /**
     * 文件路径
     */
    @FastDfsColumn(index = 3, dynamicField = DynamicFieldType.allRestByte)
    private String path;

    /**
     * 构造函数
     *
     * @param inputStream 输入流
     * @param fileSize    文件大小
     * @param path        文件路径
     * @param fileOffset  开始位置
     */
    public ModifyRequest(InputStream inputStream, long fileSize, String path, long fileOffset) {
        super();
        this.inputFile = inputStream;
        this.fileSize = fileSize;
        this.path = path;
        this.fileOffset = fileOffset;
        head = new ProtocolHead(CmdConstants.STORAGE_PROTO_CMD_MODIFY_FILE);

    }

    /**
     * 打包参数
     */
    @Override
    public byte[] encodeParam(Charset charset) {
        // 运行时参数在此计算值
        this.pathSize = path.getBytes(charset).length;
        return super.encodeParam(charset);
    }

    public long getPathSize() {
        return pathSize;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public String getPath() {
        return path;
    }

    @Override
    public long getFileSize() {
        return fileSize;
    }

}

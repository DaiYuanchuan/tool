package cn.novelweb.tool.upload.fastdfs.protocol.storage.request;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.DynamicFieldType;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.ProtocolHead;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * <p>文件上传请求</p>
 * <p>2020-02-03 16:52</p>
 *
 * @author LiZW
 **/
public class AppendFileRequest extends BaseRequest {

    /**
     * 文件路径长度
     */
    @FastDfsColumn(index = 0)
    private long pathSize;

    /**
     * 发送文件长度
     */
    @FastDfsColumn(index = 1)
    private long fileSize;

    /**
     * 文件路径
     */
    @FastDfsColumn(index = 2, dynamicField = DynamicFieldType.allRestByte)
    private String path;

    /**
     * 构造函数
     *
     * @param inputStream 文件输入流
     * @param fileSize    文件大小
     * @param path        文件路径
     */
    public AppendFileRequest(InputStream inputStream, long fileSize, String path) {
        super();
        this.inputFile = inputStream;
        this.fileSize = fileSize;
        this.path = path;
        head = new ProtocolHead(CmdConstants.STORAGE_PROTO_CMD_APPEND_FILE);
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

    public void setPathSize(long pathSize) {
        this.pathSize = pathSize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public String toString() {
        return "AppendFileRequest [pathSize=" + pathSize + ", fileSize=" + fileSize + ", path=" + path + "]";
    }

}

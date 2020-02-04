package cn.novelweb.tool.upload.fastdfs.protocol.storage.request;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.ProtocolHead;

import java.io.InputStream;

/**
 * <p></p>
 * <p>2020-02-03 16:57</p>
 *
 * @author Dai Yuanchuan
 **/
public class UploadFileRequest extends BaseRequest {

    /**
     * 存储节点index
     */
    @FastDfsColumn(index = 0)
    private byte storeIndex;

    /**
     * 发送文件长度
     */
    @FastDfsColumn(index = 1)
    private long fileSize;

    /**
     * 文件扩展名
     */
    @FastDfsColumn(index = 2, max = OtherConstants.DFS_FILE_EXT_NAME_MAX_LEN)
    private String fileExtName;

    /**
     * 构造函数
     *
     * @param storeIndex     存储节点
     * @param inputStream    输入流
     * @param fileExtName    文件扩展名
     * @param fileSize       文件大小
     * @param isAppenderFile 是否支持断点续传
     */
    public UploadFileRequest(byte storeIndex, InputStream inputStream, String fileExtName, long fileSize, boolean isAppenderFile) {
        super();
        this.inputFile = inputStream;
        this.fileSize = fileSize;
        this.storeIndex = storeIndex;
        this.fileExtName = fileExtName;
        if (isAppenderFile) {
            head = new ProtocolHead(CmdConstants.STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE);
        } else {
            head = new ProtocolHead(CmdConstants.STORAGE_PROTO_CMD_UPLOAD_FILE);
        }
    }

    public byte getStoreIndex() {
        return storeIndex;
    }

    public void setStoreIndex(byte storeIndex) {
        this.storeIndex = storeIndex;
    }

    public String getFileExtName() {
        return fileExtName;
    }

    public void setFileExtName(String fileExtName) {
        this.fileExtName = fileExtName;
    }

    @Override
    public long getFileSize() {
        return fileSize;
    }
}


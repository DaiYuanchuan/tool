package cn.novelweb.tool.upload.fastdfs.protocol.storage.request;

import cn.novelweb.tool.upload.fastdfs.constant.CmdConstants;
import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.DynamicFieldType;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.ProtocolHead;

import java.io.InputStream;

/**
 * <p>从文件上传命令</p>
 * <p>2020-02-03 16:59</p>
 *
 * @author Dai Yuanchuan
 **/
public class UploadSlaveFileRequest extends BaseRequest {

    /**
     * 主文件名长度
     */
    @FastDfsColumn(index = 0)
    private long masterFileNameSize;

    /**
     * 发送文件长度
     */
    @FastDfsColumn(index = 1)
    private long fileSize;

    /**
     * 名称前缀
     */
    @FastDfsColumn(index = 2, max = OtherConstants.DFS_FILE_PREFIX_MAX_LEN)
    private final String prefixName;

    /**
     * 文件扩展名
     */
    @FastDfsColumn(index = 3, max = OtherConstants.DFS_FILE_EXT_NAME_MAX_LEN)
    private String fileExtName;

    /**
     * 主文件名
     */
    @FastDfsColumn(index = 4, dynamicField = DynamicFieldType.allRestByte)
    private final String masterFilename;

    /**
     * 构造函数
     *
     * @param inputStream    输入流
     * @param fileSize       文件大小
     * @param masterFilename 主文件名称(路径)
     * @param prefixName     从文件前缀
     * @param fileExtName    文件扩展名
     */
    public UploadSlaveFileRequest(InputStream inputStream, long fileSize, String masterFilename, String prefixName, String fileExtName) {
        super();
        this.inputFile = inputStream;
        this.fileSize = fileSize;
        this.masterFileNameSize = masterFilename.length();
        this.masterFilename = masterFilename;
        this.fileExtName = fileExtName;
        this.prefixName = prefixName;
        head = new ProtocolHead(CmdConstants.STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE);
    }

    public long getMasterFileNameSize() {
        return masterFileNameSize;
    }

    public void setMasterFileNameSize(long masterFileNameSize) {
        this.masterFileNameSize = masterFileNameSize;
    }

    public String getPrefixName() {
        return prefixName;
    }

    public String getMasterFilename() {
        return masterFilename;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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

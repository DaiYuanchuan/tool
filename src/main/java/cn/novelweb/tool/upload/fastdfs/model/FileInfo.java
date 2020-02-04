package cn.novelweb.tool.upload.fastdfs.model;

import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * <p>上传文件信息</p>
 * <p>2016/11/20 11:05</p>
 *
 * @author LiZW
 **/
public class FileInfo implements Serializable {

    /**
     * 长度
     */
    @FastDfsColumn(index = 0)
    private long fileSize;

    /**
     * 创建时间
     */
    @FastDfsColumn(index = 1)
    private int createTime;

    /**
     * 校验码
     */
    @FastDfsColumn(index = 2)
    private int crc32;

    /**
     * ip地址
     */
    @FastDfsColumn(index = 3, max = OtherConstants.DFS_IP_ADDR_SIZE)
    private String sourceIpAddr;

    public FileInfo() {
        super();
    }

    /**
     * @param sourceIpAddr 文件IP地址
     * @param fileSize     文件大小
     * @param createTime   创建时间
     * @param crc32        校验码
     */
    public FileInfo(String sourceIpAddr, long fileSize, int createTime, int crc32) {
        super();
        this.sourceIpAddr = sourceIpAddr;
        this.fileSize = fileSize;
        this.createTime = createTime;
        this.crc32 = crc32;
    }

    /**
     * @return the sourceIpAddr
     */
    public String getSourceIpAddr() {
        return sourceIpAddr;
    }

    /**
     * @param sourceIpAddr the sourceIpAddr to set
     */
    public void setSourceIpAddr(String sourceIpAddr) {
        this.sourceIpAddr = sourceIpAddr;
    }

    /**
     * @return the size
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize the size to set
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return the createTime
     */
    public int getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    /**
     * @return the crc32
     */
    public int getCrc32() {
        return crc32;
    }

    /**
     * @param crc32 the crc32 to set
     */
    public void setCrc32(int crc32) {
        this.crc32 = crc32;
    }

    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "FileInfo{" +
                "fileSize=" + fileSize +
                ", createTime=" + df.format(createTime) +
                ", crc32=" + crc32 +
                ", sourceIpAddr='" + sourceIpAddr + '\'' +
                '}';
    }
}

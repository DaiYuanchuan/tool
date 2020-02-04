package cn.novelweb.tool.upload.fastdfs.model;

import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>FastDFS中storage节点的状态信息</p>
 * <p>2020-02-03 16:07</p>
 *
 * @author LiZW
 **/
public class StorageState implements Serializable {

    /**
     * 状态代码
     */
    @FastDfsColumn(index = 0)
    private byte status;

    /**
     * id
     */
    @FastDfsColumn(index = 1, max = OtherConstants.DFS_STORAGE_ID_MAX_SIZE)
    private String id;

    /**
     * ip地址
     */
    @FastDfsColumn(index = 2, max = OtherConstants.DFS_IP_ADDR_SIZE)
    private String ipAddr;

    /**
     * http domain name
     */
    @FastDfsColumn(index = 3, max = OtherConstants.DFS_DOMAIN_NAME_MAX_SIZE)
    private String domainName;

    /**
     * 源ip地址
     */
    @FastDfsColumn(index = 4, max = OtherConstants.DFS_IP_ADDR_SIZE)
    private String srcIpAddr;

    /**
     * version
     */
    @FastDfsColumn(index = 5, max = OtherConstants.DFS_VERSION_SIZE)
    private String version;

    /**
     * 存储加入时间
     * storage join timestamp (create timestamp)
     */
    @FastDfsColumn(index = 6)
    private Date joinTime;

    /**
     * 存储更新时间
     * storage service started timestamp
     */
    @FastDfsColumn(index = 7)
    private Date upTime;

    /**
     * 存储总容量
     * toTal disk storage in Mb
     */
    @FastDfsColumn(index = 8)
    private long toTalMb;

    /**
     * 空闲存储
     * free disk storage in Mb
     */
    @FastDfsColumn(index = 9)
    private long freeMb;

    /**
     * 文件上传权重
     * upload priority
     */
    @FastDfsColumn(index = 10)
    private int uploadPriority;

    /**
     * 存储路径数
     * store base path count of each storage
     */
    @FastDfsColumn(index = 11)
    private int storePathCount;

    /**
     * 存储路径子目录数
     */
    @FastDfsColumn(index = 12)
    private int subDirCountPerPath;

    /**
     * 当前写路径
     * current write path index
     */
    @FastDfsColumn(index = 13)
    private int currentWritePath;

    /**
     * 存储端口
     */
    @FastDfsColumn(index = 14)
    private int storagePort;

    /**
     * 存储http端口
     * storage http server port
     */
    @FastDfsColumn(index = 15)
    private int storageHttpPort;

    @FastDfsColumn(index = 16, max = OtherConstants.DFS_PROTO_CONNECTION_LEN)
    private int connectionAllocCount;

    @FastDfsColumn(index = 17, max = OtherConstants.DFS_PROTO_CONNECTION_LEN)
    private int connectionCurrentCount;

    @FastDfsColumn(index = 18, max = OtherConstants.DFS_PROTO_CONNECTION_LEN)
    private int connectionMaxCount;

    /**
     * 总上传文件数
     */
    @FastDfsColumn(index = 19)
    private long toTalUploadCount;

    /**
     * 成功上传文件数
     */
    @FastDfsColumn(index = 20)
    private long successUploadCount;

    /**
     * 合并存储文件数
     */
    @FastDfsColumn(index = 21)
    private long toTalAppendCount;

    /**
     * 成功合并文件数
     */
    @FastDfsColumn(index = 22)
    private long successAppendCount;

    /**
     * 文件修改数
     */
    @FastDfsColumn(index = 23)
    private long toTalModifyCount;

    /**
     * 文件成功修改数
     */
    @FastDfsColumn(index = 24)
    private long successModifyCount;

    /**
     * 总清除数
     */
    @FastDfsColumn(index = 25)
    private long toTalTruncateCount;

    /**
     * 成功清除数
     */
    @FastDfsColumn(index = 26)
    private long successTruncateCount;

    /**
     * 总设置标签数
     */
    @FastDfsColumn(index = 27)
    private long toTalSetMetaCount;

    /**
     * 成功设置标签数
     */
    @FastDfsColumn(index = 28)
    private long successSetMetaCount;

    /**
     * 总删除文件数
     */
    @FastDfsColumn(index = 29)
    private long toTalDeleteCount;

    /**
     * 成功删除文件数
     */
    @FastDfsColumn(index = 30)
    private long successDeleteCount;

    /**
     * 总下载量
     */
    @FastDfsColumn(index = 31)
    private long toTalDownloadCount;

    /**
     * 成功下载量
     */
    @FastDfsColumn(index = 32)
    private long successDownloadCount;

    /**
     * 总获取标签数
     */
    @FastDfsColumn(index = 33)
    private long toTalGetMetaCount;

    /**
     * 成功获取标签数
     */
    @FastDfsColumn(index = 34)
    private long successGetMetaCount;

    /**
     * 总创建链接数
     */
    @FastDfsColumn(index = 35)
    private long toTalCreateLinkCount;

    /**
     * 成功创建链接数
     */
    @FastDfsColumn(index = 36)
    private long successCreateLinkCount;

    /**
     * 总删除链接数
     */
    @FastDfsColumn(index = 37)
    private long toTalDeleteLinkCount;

    /**
     * 成功删除链接数
     */
    @FastDfsColumn(index = 38)
    private long successDeleteLinkCount;

    /**
     * 总上传数据量
     */
    @FastDfsColumn(index = 39)
    private long toTalUploadBytes;

    /**
     * 成功上传数据量
     */
    @FastDfsColumn(index = 40)
    private long successUploadBytes;

    /**
     * 合并数据量
     */
    @FastDfsColumn(index = 41)
    private long toTalAppendBytes;

    /**
     * 成功合并数据量
     */
    @FastDfsColumn(index = 42)
    private long successAppendBytes;

    /**
     * 修改数据量
     */
    @FastDfsColumn(index = 43)
    private long toTalModifyBytes;

    /**
     * 成功修改数据量
     */
    @FastDfsColumn(index = 44)
    private long successModifyBytes;

    /**
     * 下载数据量
     */
    @FastDfsColumn(index = 45)
    private long toTalDownloadBytes;

    /**
     * 成功下载数据量
     */
    @FastDfsColumn(index = 46)
    private long successDownloadBytes;

    /**
     * 同步数据量
     */
    @FastDfsColumn(index = 47)
    private long toTalSyncInBytes;

    /**
     * 成功同步数据量
     */
    @FastDfsColumn(index = 48)
    private long successSyncInBytes;

    /**
     * 同步输出数据量
     */
    @FastDfsColumn(index = 49)
    private long toTalSyncOutBytes;

    /**
     * 成功同步输出数据量
     */
    @FastDfsColumn(index = 50)
    private long successSyncOutBytes;

    /**
     * 打开文件数量
     */
    @FastDfsColumn(index = 51)
    private long toTalFileOpenCount;

    /**
     * 成功打开文件数量
     */
    @FastDfsColumn(index = 52)
    private long successFileOpenCount;

    /**
     * 文件读取数量
     */
    @FastDfsColumn(index = 53)
    private long toTalFileReadCount;

    /**
     * 文件成功读取数量
     */
    @FastDfsColumn(index = 54)
    private long successFileReadCount;

    /**
     * 文件写数量
     */
    @FastDfsColumn(index = 56)
    private long toTalFileWriteCount;

    /**
     * 文件成功写数量
     */
    @FastDfsColumn(index = 57)
    private long successFileWriteCount;

    /**
     * 最后上传时间
     */
    @FastDfsColumn(index = 58)
    private Date lastSourceUpdate;

    /**
     * 最后同步时间
     */
    @FastDfsColumn(index = 59)
    private Date lastSyncUpdate;

    /**
     * 最后同步时间戳
     */
    @FastDfsColumn(index = 60)
    private Date lastSyncedTimestamp;

    /**
     * 最后心跳时间
     */
    @FastDfsColumn(index = 61)
    private Date lastHeartBeatTime;

    /**
     * 是否trunk服务器
     */
    @FastDfsColumn(index = 62)
    private boolean isTrunkServer;

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getSrcIpAddr() {
        return srcIpAddr;
    }

    public void setSrcIpAddr(String srcIpAddr) {
        this.srcIpAddr = srcIpAddr;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Date joinTime) {
        this.joinTime = joinTime;
    }

    public Date getUpTime() {
        return upTime;
    }

    public void setUpTime(Date upTime) {
        this.upTime = upTime;
    }

    public long getToTalMb() {
        return toTalMb;
    }

    public void setToTalMb(long toTalMb) {
        this.toTalMb = toTalMb;
    }

    public long getFreeMb() {
        return freeMb;
    }

    public void setFreeMb(long freeMb) {
        this.freeMb = freeMb;
    }

    public int getUploadPriority() {
        return uploadPriority;
    }

    public void setUploadPriority(int uploadPriority) {
        this.uploadPriority = uploadPriority;
    }

    public int getStorePathCount() {
        return storePathCount;
    }

    public void setStorePathCount(int storePathCount) {
        this.storePathCount = storePathCount;
    }

    public int getSubDirCountPerPath() {
        return subDirCountPerPath;
    }

    public void setSubDirCountPerPath(int subDirCountPerPath) {
        this.subDirCountPerPath = subDirCountPerPath;
    }

    public int getCurrentWritePath() {
        return currentWritePath;
    }

    public void setCurrentWritePath(int currentWritePath) {
        this.currentWritePath = currentWritePath;
    }

    public int getStoragePort() {
        return storagePort;
    }

    public void setStoragePort(int storagePort) {
        this.storagePort = storagePort;
    }

    public int getStorageHttpPort() {
        return storageHttpPort;
    }

    public void setStorageHttpPort(int storageHttpPort) {
        this.storageHttpPort = storageHttpPort;
    }

    public int getConnectionAllocCount() {
        return connectionAllocCount;
    }

    public void setConnectionAllocCount(int connectionAllocCount) {
        this.connectionAllocCount = connectionAllocCount;
    }

    public int getConnectionCurrentCount() {
        return connectionCurrentCount;
    }

    public void setConnectionCurrentCount(int connectionCurrentCount) {
        this.connectionCurrentCount = connectionCurrentCount;
    }

    public int getConnectionMaxCount() {
        return connectionMaxCount;
    }

    public void setConnectionMaxCount(int connectionMaxCount) {
        this.connectionMaxCount = connectionMaxCount;
    }

    public long getToTalUploadCount() {
        return toTalUploadCount;
    }

    public void setToTalUploadCount(long toTalUploadCount) {
        this.toTalUploadCount = toTalUploadCount;
    }

    public long getSuccessUploadCount() {
        return successUploadCount;
    }

    public void setSuccessUploadCount(long successUploadCount) {
        this.successUploadCount = successUploadCount;
    }

    public long getToTalAppendCount() {
        return toTalAppendCount;
    }

    public void setToTalAppendCount(long toTalAppendCount) {
        this.toTalAppendCount = toTalAppendCount;
    }

    public long getSuccessAppendCount() {
        return successAppendCount;
    }

    public void setSuccessAppendCount(long successAppendCount) {
        this.successAppendCount = successAppendCount;
    }

    public long getToTalModifyCount() {
        return toTalModifyCount;
    }

    public void setToTalModifyCount(long toTalModifyCount) {
        this.toTalModifyCount = toTalModifyCount;
    }

    public long getSuccessModifyCount() {
        return successModifyCount;
    }

    public void setSuccessModifyCount(long successModifyCount) {
        this.successModifyCount = successModifyCount;
    }

    public long getToTalTruncateCount() {
        return toTalTruncateCount;
    }

    public void setToTalTruncateCount(long toTalTruncateCount) {
        this.toTalTruncateCount = toTalTruncateCount;
    }

    public long getSuccessTruncateCount() {
        return successTruncateCount;
    }

    public void setSuccessTruncateCount(long successTruncateCount) {
        this.successTruncateCount = successTruncateCount;
    }

    public long getToTalSetMetaCount() {
        return toTalSetMetaCount;
    }

    public void setToTalSetMetaCount(long toTalSetMetaCount) {
        this.toTalSetMetaCount = toTalSetMetaCount;
    }

    public long getSuccessSetMetaCount() {
        return successSetMetaCount;
    }

    public void setSuccessSetMetaCount(long successSetMetaCount) {
        this.successSetMetaCount = successSetMetaCount;
    }

    public long getToTalDeleteCount() {
        return toTalDeleteCount;
    }

    public void setToTalDeleteCount(long toTalDeleteCount) {
        this.toTalDeleteCount = toTalDeleteCount;
    }

    public long getSuccessDeleteCount() {
        return successDeleteCount;
    }

    public void setSuccessDeleteCount(long successDeleteCount) {
        this.successDeleteCount = successDeleteCount;
    }

    public long getToTalDownloadCount() {
        return toTalDownloadCount;
    }

    public void setToTalDownloadCount(long toTalDownloadCount) {
        this.toTalDownloadCount = toTalDownloadCount;
    }

    public long getSuccessDownloadCount() {
        return successDownloadCount;
    }

    public void setSuccessDownloadCount(long successDownloadCount) {
        this.successDownloadCount = successDownloadCount;
    }

    public long getToTalGetMetaCount() {
        return toTalGetMetaCount;
    }

    public void setToTalGetMetaCount(long toTalGetMetaCount) {
        this.toTalGetMetaCount = toTalGetMetaCount;
    }

    public long getSuccessGetMetaCount() {
        return successGetMetaCount;
    }

    public void setSuccessGetMetaCount(long successGetMetaCount) {
        this.successGetMetaCount = successGetMetaCount;
    }

    public long getToTalCreateLinkCount() {
        return toTalCreateLinkCount;
    }

    public void setToTalCreateLinkCount(long toTalCreateLinkCount) {
        this.toTalCreateLinkCount = toTalCreateLinkCount;
    }

    public long getSuccessCreateLinkCount() {
        return successCreateLinkCount;
    }

    public void setSuccessCreateLinkCount(long successCreateLinkCount) {
        this.successCreateLinkCount = successCreateLinkCount;
    }

    public long getToTalDeleteLinkCount() {
        return toTalDeleteLinkCount;
    }

    public void setToTalDeleteLinkCount(long toTalDeleteLinkCount) {
        this.toTalDeleteLinkCount = toTalDeleteLinkCount;
    }

    public long getSuccessDeleteLinkCount() {
        return successDeleteLinkCount;
    }

    public void setSuccessDeleteLinkCount(long successDeleteLinkCount) {
        this.successDeleteLinkCount = successDeleteLinkCount;
    }

    public long getToTalUploadBytes() {
        return toTalUploadBytes;
    }

    public void setToTalUploadBytes(long toTalUploadBytes) {
        this.toTalUploadBytes = toTalUploadBytes;
    }

    public long getSuccessUploadBytes() {
        return successUploadBytes;
    }

    public void setSuccessUploadBytes(long successUploadBytes) {
        this.successUploadBytes = successUploadBytes;
    }

    public long getToTalAppendBytes() {
        return toTalAppendBytes;
    }

    public void setToTalAppendBytes(long toTalAppendBytes) {
        this.toTalAppendBytes = toTalAppendBytes;
    }

    public long getSuccessAppendBytes() {
        return successAppendBytes;
    }

    public void setSuccessAppendBytes(long successAppendBytes) {
        this.successAppendBytes = successAppendBytes;
    }

    public long getToTalModifyBytes() {
        return toTalModifyBytes;
    }

    public void setToTalModifyBytes(long toTalModifyBytes) {
        this.toTalModifyBytes = toTalModifyBytes;
    }

    public long getSuccessModifyBytes() {
        return successModifyBytes;
    }

    public void setSuccessModifyBytes(long successModifyBytes) {
        this.successModifyBytes = successModifyBytes;
    }

    public long getToTalDownloadBytes() {
        return toTalDownloadBytes;
    }

    public void setToTalDownloadBytes(long toTalDownloadBytes) {
        this.toTalDownloadBytes = toTalDownloadBytes;
    }

    public long getSuccessDownloadBytes() {
        return successDownloadBytes;
    }

    public void setSuccessDownloadBytes(long successDownloadBytes) {
        this.successDownloadBytes = successDownloadBytes;
    }

    public long getToTalSyncInBytes() {
        return toTalSyncInBytes;
    }

    public void setToTalSyncInBytes(long toTalSyncInBytes) {
        this.toTalSyncInBytes = toTalSyncInBytes;
    }

    public long getSuccessSyncInBytes() {
        return successSyncInBytes;
    }

    public void setSuccessSyncInBytes(long successSyncInBytes) {
        this.successSyncInBytes = successSyncInBytes;
    }

    public long getToTalSyncOutBytes() {
        return toTalSyncOutBytes;
    }

    public void setToTalSyncOutBytes(long toTalSyncOutBytes) {
        this.toTalSyncOutBytes = toTalSyncOutBytes;
    }

    public long getSuccessSyncOutBytes() {
        return successSyncOutBytes;
    }

    public void setSuccessSyncOutBytes(long successSyncOutBytes) {
        this.successSyncOutBytes = successSyncOutBytes;
    }

    public long getToTalFileOpenCount() {
        return toTalFileOpenCount;
    }

    public void setToTalFileOpenCount(long toTalFileOpenCount) {
        this.toTalFileOpenCount = toTalFileOpenCount;
    }

    public long getSuccessFileOpenCount() {
        return successFileOpenCount;
    }

    public void setSuccessFileOpenCount(long successFileOpenCount) {
        this.successFileOpenCount = successFileOpenCount;
    }

    public long getToTalFileReadCount() {
        return toTalFileReadCount;
    }

    public void setToTalFileReadCount(long toTalFileReadCount) {
        this.toTalFileReadCount = toTalFileReadCount;
    }

    public long getSuccessFileReadCount() {
        return successFileReadCount;
    }

    public void setSuccessFileReadCount(long successFileReadCount) {
        this.successFileReadCount = successFileReadCount;
    }

    public long getToTalFileWriteCount() {
        return toTalFileWriteCount;
    }

    public void setToTalFileWriteCount(long toTalFileWriteCount) {
        this.toTalFileWriteCount = toTalFileWriteCount;
    }

    public long getSuccessFileWriteCount() {
        return successFileWriteCount;
    }

    public void setSuccessFileWriteCount(long successFileWriteCount) {
        this.successFileWriteCount = successFileWriteCount;
    }

    public Date getLastSourceUpdate() {
        return lastSourceUpdate;
    }

    public void setLastSourceUpdate(Date lastSourceUpdate) {
        this.lastSourceUpdate = lastSourceUpdate;
    }

    public Date getLastSyncUpdate() {
        return lastSyncUpdate;
    }

    public void setLastSyncUpdate(Date lastSyncUpdate) {
        this.lastSyncUpdate = lastSyncUpdate;
    }

    public Date getLastSyncedTimestamp() {
        return lastSyncedTimestamp;
    }

    public void setLastSyncedTimestamp(Date lastSyncedTimestamp) {
        this.lastSyncedTimestamp = lastSyncedTimestamp;
    }

    public Date getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }

    public void setLastHeartBeatTime(Date lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }

    public boolean isTrunkServer() {
        return isTrunkServer;
    }

    public void setTrunkServer(boolean trunkServer) {
        isTrunkServer = trunkServer;
    }

    @Override
    public String toString() {
        return "StorageState{" +
                "status=" + status +
                ", id='" + id + '\'' +
                ", ipAddr='" + ipAddr + '\'' +
                ", domainName='" + domainName + '\'' +
                ", srcIpAddr='" + srcIpAddr + '\'' +
                ", version='" + version + '\'' +
                ", joinTime=" + joinTime +
                ", upTime=" + upTime +
                ", toTalMb=" + toTalMb +
                ", freeMb=" + freeMb +
                ", uploadPriority=" + uploadPriority +
                ", storePathCount=" + storePathCount +
                ", subDirCountPerPath=" + subDirCountPerPath +
                ", currentWritePath=" + currentWritePath +
                ", storagePort=" + storagePort +
                ", storageHttpPort=" + storageHttpPort +
                ", connectionAllocCount=" + connectionAllocCount +
                ", connectionCurrentCount=" + connectionCurrentCount +
                ", connectionMaxCount=" + connectionMaxCount +
                ", toTalUploadCount=" + toTalUploadCount +
                ", successUploadCount=" + successUploadCount +
                ", toTalAppendCount=" + toTalAppendCount +
                ", successAppendCount=" + successAppendCount +
                ", toTalModifyCount=" + toTalModifyCount +
                ", successModifyCount=" + successModifyCount +
                ", toTalTruncateCount=" + toTalTruncateCount +
                ", successTruncateCount=" + successTruncateCount +
                ", toTalSetMetaCount=" + toTalSetMetaCount +
                ", successSetMetaCount=" + successSetMetaCount +
                ", toTalDeleteCount=" + toTalDeleteCount +
                ", successDeleteCount=" + successDeleteCount +
                ", toTalDownloadCount=" + toTalDownloadCount +
                ", successDownloadCount=" + successDownloadCount +
                ", toTalGetMetaCount=" + toTalGetMetaCount +
                ", successGetMetaCount=" + successGetMetaCount +
                ", toTalCreateLinkCount=" + toTalCreateLinkCount +
                ", successCreateLinkCount=" + successCreateLinkCount +
                ", toTalDeleteLinkCount=" + toTalDeleteLinkCount +
                ", successDeleteLinkCount=" + successDeleteLinkCount +
                ", toTalUploadBytes=" + toTalUploadBytes +
                ", successUploadBytes=" + successUploadBytes +
                ", toTalAppendBytes=" + toTalAppendBytes +
                ", successAppendBytes=" + successAppendBytes +
                ", toTalModifyBytes=" + toTalModifyBytes +
                ", successModifyBytes=" + successModifyBytes +
                ", toTalDownloadBytes=" + toTalDownloadBytes +
                ", successDownloadBytes=" + successDownloadBytes +
                ", toTalSyncInBytes=" + toTalSyncInBytes +
                ", successSyncInBytes=" + successSyncInBytes +
                ", toTalSyncOutBytes=" + toTalSyncOutBytes +
                ", successSyncOutBytes=" + successSyncOutBytes +
                ", toTalFileOpenCount=" + toTalFileOpenCount +
                ", successFileOpenCount=" + successFileOpenCount +
                ", toTalFileReadCount=" + toTalFileReadCount +
                ", successFileReadCount=" + successFileReadCount +
                ", toTalFileWriteCount=" + toTalFileWriteCount +
                ", successFileWriteCount=" + successFileWriteCount +
                ", lastSourceUpdate=" + lastSourceUpdate +
                ", lastSyncUpdate=" + lastSyncUpdate +
                ", lastSyncedTimestamp=" + lastSyncedTimestamp +
                ", lastHeartBeatTime=" + lastHeartBeatTime +
                ", isTrunkServer=" + isTrunkServer +
                '}';
    }

}

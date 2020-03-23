package cn.novelweb.tool.upload.fastdfs.model;

import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;

import java.io.Serializable;

/**
 * <p>FastDFS中group的状态信息</p>
 * <p>2020-02-03 16:00</p>
 *
 * @author LiZW
 **/
public class GroupState implements Serializable {

    /**
     * group名称
     */
    @FastDfsColumn(index = 0, max = OtherConstants.DFS_GROUP_NAME_MAX_LEN + 1)
    private String groupName;

    /**
     * group总计存储容量(Mb)
     */
    @FastDfsColumn(index = 1)
    private long totalMb;

    /**
     * group空闲存储容量(Mb)
     */
    @FastDfsColumn(index = 2)
    private long freeMb;

    /**
     * trunk free space in Mb
     */
    @FastDfsColumn(index = 3)
    private long trunkFreeMb;

    /**
     * storage server count
     */
    @FastDfsColumn(index = 4)
    private int storageCount;

    /**
     * storage server port
     */
    @FastDfsColumn(index = 5)
    private int storagePort;

    /**
     * storage server HTTP port
     */
    @FastDfsColumn(index = 6)
    private int storageHttpPort;

    /**
     * active storage server count
     */
    @FastDfsColumn(index = 7)
    private int activeCount;

    /**
     * current storage server index to upload file
     */
    @FastDfsColumn(index = 8)
    private int currentWriteServer;

    /**
     * store base path count of each storage server
     */
    @FastDfsColumn(index = 9)
    private int storePathCount;

    /**
     * sub dir count per store path
     */
    @FastDfsColumn(index = 10)
    private int subDirCountPerPath;

    /**
     * current trunk file id
     */
    @FastDfsColumn(index = 11)
    private int currentTrunkFileId;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getTotalMb() {
        return totalMb;
    }

    public void setTotalMb(long totalMb) {
        this.totalMb = totalMb;
    }

    public long getFreeMb() {
        return freeMb;
    }

    public void setFreeMb(long freeMb) {
        this.freeMb = freeMb;
    }

    public long getTrunkFreeMb() {
        return trunkFreeMb;
    }

    public void setTrunkFreeMb(long trunkFreeMb) {
        this.trunkFreeMb = trunkFreeMb;
    }

    public int getStorageCount() {
        return storageCount;
    }

    public void setStorageCount(int storageCount) {
        this.storageCount = storageCount;
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

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getCurrentWriteServer() {
        return currentWriteServer;
    }

    public void setCurrentWriteServer(int currentWriteServer) {
        this.currentWriteServer = currentWriteServer;
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

    public int getCurrentTrunkFileId() {
        return currentTrunkFileId;
    }

    public void setCurrentTrunkFileId(int currentTrunkFileId) {
        this.currentTrunkFileId = currentTrunkFileId;
    }

    @Override
    public String toString() {
        return "GroupState{" +
                "groupName='" + groupName + '\'' +
                ", totalMb=" + totalMb +
                ", freeMb=" + freeMb +
                ", trunkFreeMb=" + trunkFreeMb +
                ", storageCount=" + storageCount +
                ", storagePort=" + storagePort +
                ", storageHttpPort=" + storageHttpPort +
                ", activeCount=" + activeCount +
                ", currentWriteServer=" + currentWriteServer +
                ", storePathCount=" + storePathCount +
                ", subDirCountPerPath=" + subDirCountPerPath +
                ", currentTrunkFileId=" + currentTrunkFileId +
                '}';
    }
}

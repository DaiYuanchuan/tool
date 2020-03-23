package cn.novelweb.tool.upload.fastdfs.model;

import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.mapper.FastDfsColumn;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * <p>向tracker请求上传、下载文件或其他文件请求时，tracker返回的文件storage节点的信息</p>
 * <p>2020-02-03 16:05</p>
 *
 * @author LiZW
 **/
public class StorageNode implements Serializable {

    @FastDfsColumn(index = 0, max = OtherConstants.DFS_GROUP_NAME_MAX_LEN)
    private String groupName;

    @FastDfsColumn(index = 1, max = OtherConstants.DFS_IP_ADDR_SIZE - 1)
    private String ip;

    @FastDfsColumn(index = 2)
    private int port;

    @FastDfsColumn(index = 3)
    private byte storeIndex;

    /**
     * 存储节点
     *
     * @param ip         存储服务器IP地址
     * @param port       存储服务器端口号
     * @param storeIndex 存储服务器顺序
     */
    public StorageNode(String ip, int port, byte storeIndex) {
        super();
        this.ip = ip;
        this.port = port;
        this.storeIndex = storeIndex;
    }

    public StorageNode() {
        super();
    }

    /**
     * @return 根据IP和端口 返回 InetSocketAddress 对象
     */
    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(ip, port);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public byte getStoreIndex() {
        return storeIndex;
    }

    public void setStoreIndex(byte storeIndex) {
        this.storeIndex = storeIndex;
    }

    @Override
    public String toString() {
        return "StorageNode{" +
                "groupName='" + groupName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", storeIndex=" + storeIndex +
                '}';
    }

}

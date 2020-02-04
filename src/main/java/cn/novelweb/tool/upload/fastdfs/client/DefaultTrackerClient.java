package cn.novelweb.tool.upload.fastdfs.client;

import cn.novelweb.tool.upload.fastdfs.conn.CommandExecutor;
import cn.novelweb.tool.upload.fastdfs.exception.FastDfsException;
import cn.novelweb.tool.upload.fastdfs.model.GroupState;
import cn.novelweb.tool.upload.fastdfs.model.StorageNode;
import cn.novelweb.tool.upload.fastdfs.model.StorageNodeInfo;
import cn.novelweb.tool.upload.fastdfs.model.StorageState;
import cn.novelweb.tool.upload.fastdfs.protocol.tracker.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 目录服务(Tracker)客户端接口 默认实现<br/>
 * <b>注意: 当前类最好使用单例，一个应用只需要一个实例</b>
 * <p>2020-02-03 17:31</p>
 *
 * @author LiZW
 **/
@Slf4j
public class DefaultTrackerClient implements TrackerClient {

    private CommandExecutor commandExecutor;

    public DefaultTrackerClient(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public StorageNode getStorageNode() {
        GetStorageNodeCommandAbstract command = new GetStorageNodeCommandAbstract();
        return commandExecutor.execute(command);
    }

    @Override
    public StorageNode getStorageNode(String groupName) {
        StorageNode storageNode = null;
        GetStorageNodeCommandAbstract command = new GetStorageNodeCommandAbstract(groupName);
        try {
            storageNode = commandExecutor.execute(command);
        } catch (FastDfsException e) {
            log.error("存储节点不存在 groupName=[" + groupName + "]", e);
        }
        return storageNode;
    }

    @Override
    public StorageNodeInfo getFetchStorage(String groupName, String filename) {
        GetFetchStorageCommandAbstract command = new GetFetchStorageCommandAbstract(groupName, filename, false);
        return commandExecutor.execute(command);
    }

    @Override
    public StorageNodeInfo getFetchStorageAndUpdate(String groupName, String filename) {
        GetFetchStorageCommandAbstract command = new GetFetchStorageCommandAbstract(groupName, filename, true);
        return commandExecutor.execute(command);
    }

    @Override
    public List<GroupState> getGroupStates() {
        GetGroupListCommandAbstract command = new GetGroupListCommandAbstract();
        List<GroupState> result = commandExecutor.execute(command);
        return result != null ? result : new ArrayList<GroupState>();
    }

    @Override
    public List<StorageState> getStorageStates(String groupName) {
        GetStorageListCommandAbstract command = new GetStorageListCommandAbstract(groupName);
        List<StorageState> result = commandExecutor.execute(command);
        return result != null ? result : new ArrayList<StorageState>();
    }

    @Override
    public StorageState getStorageState(String groupName, String storageIp) {
        GetStorageListCommandAbstract command = new GetStorageListCommandAbstract(groupName, storageIp);
        List<StorageState> result = commandExecutor.execute(command);
        if (result != null && result.size() > 1) {
            log.warn("应该返回一条数据, 但是现在返回了{}条, 只取第一条", result.size());
        }
        return (result != null && result.size() >= 1) ? result.get(0) : null;
    }

    @Override
    public boolean deleteStorage(String groupName, String storageIp) {
        DeleteStorageCommandAbstract command = new DeleteStorageCommandAbstract(groupName, storageIp);
        try {
            commandExecutor.execute(command);
        } catch (Throwable e) {
            log.error("踢出存储服务器失败, groupName=[" + groupName + "], storageIp=[" + storageIp + "]", e);
            return false;
        }
        return true;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

}

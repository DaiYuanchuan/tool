package cn.novelweb.tool.upload.fastdfs.client;

import cn.novelweb.tool.upload.fastdfs.conn.CommandExecutor;
import cn.novelweb.tool.upload.fastdfs.constant.ErrorCodeConstants;
import cn.novelweb.tool.upload.fastdfs.exception.FastDfsServerException;
import cn.novelweb.tool.upload.fastdfs.model.*;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.*;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.callback.DownloadCallback;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.enums.StorageMetadataSetType;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 存储服务(Storage)客户端接口 默认实现<br/>
 * <b>注意: 当前类最好使用单例，一个应用只需要一个实例</b>
 * <p>2020-02-03 17:28</p>
 *
 * @author LiZW
 **/
@Slf4j
public class DefaultStorageClient implements StorageClient {

    private CommandExecutor commandExecutor;

    private TrackerClient trackerClient;

    public DefaultStorageClient(CommandExecutor commandExecutor, TrackerClient trackerClient) {
        this.commandExecutor = commandExecutor;
        this.trackerClient = trackerClient;
    }

    @Override
    public StorePath uploadFile(String groupName, InputStream inputStream, long fileSize, String fileExtName) {
        StorageNode storageNode = trackerClient.getStorageNode(groupName);
        UploadFileCommandAbstract command = new UploadFileCommandAbstract(storageNode.getStoreIndex(), inputStream, fileExtName, fileSize, false);
        return commandExecutor.execute(storageNode.getInetSocketAddress(), command);
    }

    @Override
    public StorePath uploadSlaveFile(String groupName, String masterFilename, InputStream inputStream, long fileSize, String prefixName, String fileExtName) {
        StorageNodeInfo storageNodeInfo = trackerClient.getFetchStorageAndUpdate(groupName, masterFilename);
        UploadSlaveFileCommandAbstract command = new UploadSlaveFileCommandAbstract(inputStream, fileSize, masterFilename, prefixName, fileExtName);
        return commandExecutor.execute(storageNodeInfo.getInetSocketAddress(), command);
    }

    @Override
    public Set<MateData> getMetadata(String groupName, String path) {
        try {
            StorageNodeInfo storageNodeInfo = trackerClient.getFetchStorage(groupName, path);
            GetMetadataCommandAbstract command = new GetMetadataCommandAbstract(groupName, path);
            return commandExecutor.execute(storageNodeInfo.getInetSocketAddress(), command);
        } catch (Throwable e) {
            log.error("获取文件元信息", e);
            return new HashSet<MateData>();
        }
    }

    @Override
    public boolean overwriteMetadata(String groupName, String path, Set<MateData> metaDataSet) {
        try {
            StorageNodeInfo storageNodeInfo = trackerClient.getFetchStorageAndUpdate(groupName, path);
            SetMetadataCommandAbstract command = new SetMetadataCommandAbstract(groupName, path, metaDataSet, StorageMetadataSetType.STORAGE_SET_METADATA_FLAG_OVERWRITE);
            commandExecutor.execute(storageNodeInfo.getInetSocketAddress(), command);
        } catch (Throwable e) {
            log.error("修改文件元信息（覆盖）失败", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean mergeMetadata(String groupName, String path, Set<MateData> metaDataSet) {
        try {
            StorageNodeInfo storageNodeInfo = trackerClient.getFetchStorageAndUpdate(groupName, path);
            SetMetadataCommandAbstract command = new SetMetadataCommandAbstract(groupName, path, metaDataSet, StorageMetadataSetType.STORAGE_SET_METADATA_FLAG_MERGE);
            commandExecutor.execute(storageNodeInfo.getInetSocketAddress(), command);
        } catch (Throwable e) {
            log.error("修改文件元信息（合并）失败", e);
            return false;
        }
        return true;
    }

    @Override
    public FileInfo queryFileInfo(String groupName, String path) {
        FileInfo fileInfo = null;
        StorageNodeInfo storageNodeInfo = trackerClient.getFetchStorage(groupName, path);
        QueryFileInfoCommandAbstract command = new QueryFileInfoCommandAbstract(groupName, path);
        try {
            fileInfo = commandExecutor.execute(storageNodeInfo.getInetSocketAddress(), command);
        } catch (FastDfsServerException e) {
            if (e.getErrorCode() == ErrorCodeConstants.ERR_NO_ENOENT) {
                log.error("获取文件的信息异常,ErrorCode=[{}], ErrorMessage=[{}]", e.getErrorCode(), e.getMessage());
            } else {
                throw e;
            }
        }
        return fileInfo;
    }

    @Override
    public boolean deleteFile(String groupName, String path) {
        try {
            StorageNodeInfo storageNodeInfo = trackerClient.getFetchStorageAndUpdate(groupName, path);
            DeleteFileCommandAbstract command = new DeleteFileCommandAbstract(groupName, path);
            commandExecutor.execute(storageNodeInfo.getInetSocketAddress(), command);
        } catch (Throwable e) {
            log.error("删除文件失败", e);
            return false;
        }
        return true;
    }

    @Override
    public <T> T downloadFile(String groupName, String path, DownloadCallback<T> callback) {
        long fileOffset = 0;
        long fileSize = 0;
        return downloadFile(groupName, path, fileOffset, fileSize, callback);
    }

    @Override
    public <T> T downloadFile(String groupName, String path, long fileOffset, long fileSize, DownloadCallback<T> callback) {
        StorageNodeInfo storageNodeInfo = trackerClient.getFetchStorage(groupName, path);
        DownloadFileCommandAbstract<T> command = new DownloadFileCommandAbstract<T>(groupName, path, fileOffset, fileSize, callback);
        return commandExecutor.execute(storageNodeInfo.getInetSocketAddress(), command);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public StorePath uploadFile(InputStream inputStream, long fileSize, String fileExtName, Set<MateData> metaDataSet) {
        StorageNode storageNode = trackerClient.getStorageNode();
        UploadFileCommandAbstract command = new UploadFileCommandAbstract(storageNode.getStoreIndex(), inputStream, fileExtName, fileSize, false);
        StorePath storePath = commandExecutor.execute(storageNode.getInetSocketAddress(), command);
        if (metaDataSet == null || metaDataSet.size() <= 0) {
            return storePath;
        }
        SetMetadataCommandAbstract cmd = new SetMetadataCommandAbstract(storePath.getGroup(), storePath.getPath(), metaDataSet, StorageMetadataSetType.STORAGE_SET_METADATA_FLAG_OVERWRITE);
        commandExecutor.execute(storageNode.getInetSocketAddress(), command);
        return storePath;
    }

    @Override
    public StorePath uploadAppenderFile(String groupName, InputStream inputStream, long fileSize, String fileExtName) {
        StorageNode storageNode = trackerClient.getStorageNode(groupName);
        UploadFileCommandAbstract command = new UploadFileCommandAbstract(storageNode.getStoreIndex(), inputStream, fileExtName, fileSize, true);
        return commandExecutor.execute(storageNode.getInetSocketAddress(), command);
    }

    @Override
    public void appendFile(String groupName, String path, InputStream inputStream, long fileSize) {
        StorageNodeInfo storageNodeInfo = trackerClient.getFetchStorageAndUpdate(groupName, path);
        AppendFileCommandAbstract command = new AppendFileCommandAbstract(inputStream, fileSize, path);
        commandExecutor.execute(storageNodeInfo.getInetSocketAddress(), command);
    }

    @Override
    public void modifyFile(String groupName, String path, InputStream inputStream, long fileSize, long fileOffset) {
        StorageNodeInfo storageNodeInfo = trackerClient.getFetchStorageAndUpdate(groupName, path);
        ModifyCommandAbstract command = new ModifyCommandAbstract(path, inputStream, fileSize, fileOffset);
        commandExecutor.execute(storageNodeInfo.getInetSocketAddress(), command);
    }

    @Override
    public void truncateFile(String groupName, String path, long truncatedFileSize) {
        StorageNodeInfo storageNodeInfo = trackerClient.getFetchStorageAndUpdate(groupName, path);
        TruncateCommandAbstract command = new TruncateCommandAbstract(path, truncatedFileSize);
        commandExecutor.execute(storageNodeInfo.getInetSocketAddress(), command);
    }

    @Override
    public void truncateFile(String groupName, String path) {
        long truncatedFileSize = 0;
        truncateFile(groupName, path, truncatedFileSize);
    }

    /*--------------------------------------------------------------
     *          getter、setter
     * -------------------------------------------------------------*/

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public TrackerClient getTrackerClient() {
        return trackerClient;
    }

    public void setTrackerClient(TrackerClient trackerClient) {
        this.trackerClient = trackerClient;
    }

}

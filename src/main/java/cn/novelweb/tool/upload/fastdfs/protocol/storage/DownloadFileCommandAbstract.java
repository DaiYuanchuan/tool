package cn.novelweb.tool.upload.fastdfs.protocol.storage;

import cn.novelweb.tool.upload.fastdfs.protocol.storage.callback.DownloadCallback;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.request.DownloadFileRequest;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.response.DownloadFileResponse;

/**
 * <p>下载文件</p>
 * <p>2020-02-03 17:05</p>
 *
 * @author LiZW
 **/
public class DownloadFileCommandAbstract<T> extends AbstractStorageCommand<T> {

    /**
     * 下载文件
     *
     * @param groupName  组名称
     * @param path       文件路径
     * @param fileOffset 开始位置
     * @param fileSize   读取文件长度
     * @param callback   文件下载回调
     */
    public DownloadFileCommandAbstract(String groupName, String path, long fileOffset, long fileSize, DownloadCallback<T> callback) {
        super();
        this.request = new DownloadFileRequest(groupName, path, fileOffset, fileSize);
        // 输出响应
        this.response = new DownloadFileResponse<T>(callback);
    }

    /**
     * 下载文件
     *
     * @param groupName 组名称
     * @param path      文件路径
     * @param callback  文件下载回调
     */
    public DownloadFileCommandAbstract(String groupName, String path, DownloadCallback<T> callback) {
        super();
        this.request = new DownloadFileRequest(groupName, path, 0, 0);
        // 输出响应
        this.response = new DownloadFileResponse<T>(callback);
    }

}

package cn.novelweb.tool.upload.fastdfs;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.novelweb.tool.io.FileUtils;
import cn.novelweb.tool.upload.fastdfs.callback.FastDfsUploadCompletionHandler;
import cn.novelweb.tool.upload.fastdfs.client.DefaultStorageClient;
import cn.novelweb.tool.upload.fastdfs.client.DefaultTrackerClient;
import cn.novelweb.tool.upload.fastdfs.client.StorageClient;
import cn.novelweb.tool.upload.fastdfs.client.TrackerClient;
import cn.novelweb.tool.upload.fastdfs.config.FastDfsConfig;
import cn.novelweb.tool.upload.fastdfs.conn.DefaultCommandExecutor;
import cn.novelweb.tool.upload.fastdfs.model.FileInfo;
import cn.novelweb.tool.upload.fastdfs.model.MateData;
import cn.novelweb.tool.upload.fastdfs.model.StorePath;
import cn.novelweb.tool.upload.fastdfs.pool.ConnectionPool;
import cn.novelweb.tool.upload.fastdfs.pool.PooledConnectionFactory;
import cn.novelweb.tool.upload.fastdfs.protocol.storage.callback.DownloadCallback;
import cn.novelweb.tool.upload.fastdfs.utils.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>FastDFS客户端工具</p>
 * <p>2020-02-03 15:03</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class FastDfsClient {

    private static TrackerClient trackerClient;
    private static StorageClient storageClient;

    /**
     * 未指定group时默认值
     */
    private static final String GROUP = "group1";
    private static boolean isSuccessInit = false;

    /**
     * 指定配置文件进行初始化
     *
     * @param confFileName 配置文件名称
     */
    public static void init(String confFileName) {
        FastDfsConfig.init(confFileName);
    }

    /**
     * 初始化配置
     *
     * @param fastDfsConfig 配置信息
     */
    public static void init(FastDfsConfig fastDfsConfig) {
        Log.debugLog = fastDfsConfig.getDebugLog();
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(
                fastDfsConfig.getSoTimeout(), fastDfsConfig.getConnectTimeout());
        GenericKeyedObjectPoolConfig conf = new GenericKeyedObjectPoolConfig();
        conf.setMaxTotal(fastDfsConfig.getMaxTotal());
        conf.setMaxTotalPerKey(fastDfsConfig.getMaxTotalPerKey());
        conf.setMaxIdlePerKey(fastDfsConfig.getMaxIdlePerKey());
        ConnectionPool connectionPool = new ConnectionPool(pooledConnectionFactory, conf);
        String[] split = fastDfsConfig.getTrackerServers().split(",");
        Set<String> trackerSet = new HashSet<>(split.length);
        trackerSet.addAll(Arrays.asList(split));
        DefaultCommandExecutor commandExecutor = new DefaultCommandExecutor(trackerSet, connectionPool);
        trackerClient = new DefaultTrackerClient(commandExecutor);
        storageClient = new DefaultStorageClient(commandExecutor, trackerClient);
        isSuccessInit = true;
    }

    /**
     * 上传input流
     * group默认为group1
     *
     * @param stream 需要上传的文件输入流
     * @param length 文件大小
     * @param ext    文件扩展名
     * @return 返回存储文件的路径信息
     */
    public static StorePath uploader(InputStream stream, long length, String ext) {
        return uploader(GROUP, stream, length, ext);
    }

    /**
     * 上传input流
     * 自定义group名
     *
     * @param group  组名称
     * @param stream 文件输入流
     * @param length 文件大小
     * @param ext    文件扩展名
     * @return 返回存储文件的路径信息
     */
    public static StorePath uploader(String group, InputStream stream, long length, String ext) {
        if (!successInit()) {
            return null;
        }
        return storageClient.uploadFile(group, stream, length, ext);
    }

    /**
     * 异步上传input流
     * 自定义组名称
     *
     * @param group   组名称
     * @param stream  文件输入流
     * @param length  文件大小
     * @param ext     文件扩展名
     * @param handler 异步上传完成后的回调接口,需要实现它
     */
    public static void asynchronousUpload(final String group, InputStream stream,
                                          long length, String ext,
                                          FastDfsUploadCompletionHandler handler) {
        if (!successInit()) {
            return;
        }
        ThreadUtil.execAsync(() -> {
            StorePath storePath = storageClient.uploadFile(group, stream, length, ext);
            handler.complete(storePath);
        });
    }

    /**
     * 字符串上传
     * 自定义组名称
     *
     * @param group       自定义组名
     * @param str         需要上传的字符串
     * @param ext         文件的扩展名(如:json)
     * @param charsetName 字符集(如:UTF-8)
     * @return 返回存储文件的路径信息
     */
    public static StorePath characterStringUploader(String group, String str,
                                                    String ext, String charsetName) {
        ByteArrayInputStream inputStream = IoUtil.toStream(str, charsetName);
        return uploader(group, inputStream, inputStream.available(), ext);
    }

    /**
     * 字符串上传
     * 默认使用group1
     * 默认字符集UTF-8
     *
     * @param str 需要上传的字符串
     * @param ext 文件的扩展名(如:json)
     * @return 返回存储文件的路径信息
     */
    public static StorePath characterStringUploader(String str, String ext) {
        ByteArrayInputStream inputStream = IoUtil.toStream(str, "UTF-8");
        return uploader(GROUP, inputStream, inputStream.available(), ext);
    }

    /**
     * 直接上传文件
     * group默认为group1
     *
     * @param file 需要上传的文件
     * @return 返回存储文件的路径信息
     */
    public static StorePath uploader(File file) {
        try {
            InputStream stream = new FileInputStream(file.getAbsolutePath());
            return uploader(GROUP, stream, file.length(),
                    FileUtil.extName(file.getAbsolutePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 直接上传文件
     * 自定义组的名称
     *
     * @param group 组名称
     * @param file  需要上传的文件
     * @return 返回存储文件的路径信息
     */
    public static StorePath uploader(String group, File file) {
        try {
            InputStream stream = new FileInputStream(file.getAbsolutePath());
            return uploader(group, stream, file.length(),
                    FileUtil.extName(file.getAbsolutePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除文件
     * 自定义组名称
     *
     * @param group 组名称
     * @param path  主文件路径
     * @return 返回是否删除成功的布尔值
     */
    public static boolean delete(String group, String path) {
        if (!successInit()) {
            return false;
        }
        return storageClient.deleteFile(group, path);
    }

    /**
     * 删除文件
     *
     * @param fileId 文件id(格式如:group1/M00/00/00/xxx.png)
     * @return 返回是否删除成功的布尔值
     */
    public static boolean delete(String fileId) {
        if (!successInit()) {
            return false;
        }
        int i = fileId.indexOf("/");
        return storageClient.deleteFile(fileId.substring(0, i), fileId.substring(i + 1));
    }

    /**
     * 获取文件元信息
     *
     * @param groupName 组名称
     * @param path      主文件路径
     * @return 获取文件元信息集合，不存在返回空集合
     */
    public static Set<MateData> getMetadata(String groupName, String path) {
        if (!successInit()) {
            return null;
        }
        return storageClient.getMetadata(groupName, path);
    }

    /**
     * 获取文件的信息
     *
     * @param groupName 组名称
     * @param path      主文件路径
     * @return 文件信息(不存在返回null)
     */
    public static FileInfo queryFileInfo(String groupName, String path) {
        if (!successInit()) {
            return null;
        }
        return storageClient.queryFileInfo(groupName, path);
    }

    /**
     * 下载整个文件
     *
     * @param groupName 组名称
     * @param path      主文件路径
     * @param callback  下载回调接口
     * @return 下载回调接口返回结果
     */
    public static <T> T downloadFile(String groupName, String path, DownloadCallback<T> callback) {
        if (!successInit()) {
            return null;
        }
        return storageClient.downloadFile(groupName, path, callback);
    }

    /**
     * 字符串下载
     * 将下载下的数据转为字符串
     *
     * @param groupName 组名称
     * @param path      主文件路径
     * @return 返回文本数据
     */
    public static String characterStringDownload(String groupName, String path) {
        if (!successInit()) {
            return "";
        }
        return storageClient.downloadFile(groupName, path, (InputStream inputStream) -> {
            String string = FileUtils.inputStreamToString(inputStream);
            inputStream.close();
            return string;
        });
    }

    public static TrackerClient getTrackerClient() {
        return trackerClient;
    }

    public static StorageClient getStorageClient() {
        return storageClient;
    }

    /**
     * 判断是否完成了init初始化
     *
     * @return 布尔值
     */
    private static boolean successInit() {
        if (!isSuccessInit) {
            log.error("请调用FastDfsClient.init()方法完成初始化设置");
        }
        return isSuccessInit;
    }
}

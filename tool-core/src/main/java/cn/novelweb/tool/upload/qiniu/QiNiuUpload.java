package cn.novelweb.tool.upload.qiniu;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.*;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FetchRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import com.qiniu.util.Etag;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>七牛云上传</p>
 * <p>2019-11-23 11:23</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class QiNiuUpload {

    /**
     * Region对应机房:华东
     */
    public static Region huaDong = Region.region0();

    /**
     * Region对应机房:华北
     */
    public static Region huaBei = Region.region1();

    /**
     * Region对应机房:华南
     */
    public static Region huaNan = Region.region2();

    /**
     * Region对应机房:北美
     */
    public static Region beiMei = Region.regionNa0();

    /**
     * Region对应机房:新加坡
     */
    public static Region xinJiaPo = Region.regionAs0();

    /**
     * 七牛云accessKey信息，可以在{https://portal.qiniu.com/user/key}查看
     */
    public static String accessKey;

    /**
     * 七牛云secretKey信息，可以在{https://portal.qiniu.com/user/key}查看
     */
    public static String secretKey;

    /**
     * 七牛云存储桶名称
     */
    public static String bucket;

    /**
     * 设置默认region
     */
    public static Region region = huaDong;

    /**
     * 构建七牛云文件上传管理器[支持记录上传进度]
     * 构建一个支持断点续传的上传对象。只在文件采用分片上传时才会有效。
     * 分块上传中，将每一块上传的记录保存下来。上传中断后可在上一次断点记录基础上上传剩余部分。
     * 对于不同的文件上传需要支持断点续传的情况，请定义不同的UploadManager对象，而不要共享。
     *
     * @param region   需要上传到的区域
     * @param recorder 断点记录对象[此值是文件夹，不是文件]
     * @return 返回七牛云文件上传管理器<code>UploadManager</code>类
     */
    private static UploadManager buildUploadManager(Region region, File recorder) throws IOException {
        // 构造一个带指定Region对象的配置类
        Configuration configuration = Singleton.get(Configuration.class, region);
        if (recorder == null) {
            // 构造一个带指定Zone对象的配置类
            return Singleton.get(UploadManager.class, configuration);
        }
        // 构造一个带 指定Zone对象的 和 断点记录对象的 配置类
        return Singleton.get(UploadManager.class, configuration, new FileRecorder(recorder));
    }

    /**
     * 获取七牛云桶管理器
     * 参考文档：<a href="http://developer.qiniu.com/kodo/api/rs">资源管理</a>
     *
     * @param region 需要上传到的区域
     * @return 返回七牛云桶管理器<code>BucketManager</code>类
     */
    public static BucketManager getBucketManager(Region region) {
        return Singleton.get(BucketManager.class, Auth.create(accessKey, secretKey),
                Singleton.get(Configuration.class, region));
    }

    /**
     * 解析上传成功的结果
     *
     * @param response 上传结果对象
     * @return 返回默认上传接口回复对象DefaultPutRet
     */
    private static DefaultPutRet putRet(Response response) throws QiniuException {
        DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
        if (StringUtils.isNullOrEmpty(putRet.key) || StringUtils.isNullOrEmpty(putRet.hash)) {
            return null;
        }
        return putRet;
    }

    /**
     * 服务端本地文件直接上传到七牛云
     * 大文件采用分片上传的形式
     *
     * @param cosFile      需要上传的文件对象
     * @param key          上传文件保存的文件名
     * @param upToken      上传凭证
     * @param region       需要上传到的区域
     * @param recorderFile 断点记录对象[只有断点上传有效，用来记录当前上传进度的]
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     */
    public static DefaultPutRet uploader(File cosFile, String key, String upToken, Region region, File recorderFile) throws IOException {
        if (cosFile == null) {
            return null;
        }
        return putRet(buildUploadManager(region, recorderFile).put(cosFile, key, upToken));
    }

    /**
     * 服务端本地文件直接上传到七牛云
     * 大文件采用分片上传的形式
     * 使用默认upToken、默认的region为华东
     *
     * @param cosFile 需要上传的文件对象
     * @param key     上传文件保存的文件名
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     */
    public static DefaultPutRet uploader(File cosFile, String key) throws IOException {
        return uploader(cosFile, key, getUploadToken(), region, null);
    }

    /**
     * 服务端本地文件直接上传到七牛云
     * 大文件采用分片上传的形式
     * 使用默认upToken、默认的region为华东
     *
     * @param cosFile  需要上传的文件对象
     * @param key      上传文件保存的文件名
     * @param recorder 断点记录对象[只有断点上传有效，用来记录当前上传进度的]
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     */
    public static DefaultPutRet uploader(File cosFile, String key, File recorder) throws IOException {
        return uploader(cosFile, key, getUploadToken(), region, recorder);
    }

    /**
     * 七牛云数据流文件上传
     * 适用于所有的InputStream子类
     *
     * @param cosFile 需要上传的数据流
     * @param key     上传的路径,默认不指定key的情况下，以文件内容的hash值作为文件名
     * @param upToken 七牛云上传的token
     * @param region  需要上传到的区域
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     */
    public static DefaultPutRet uploader(InputStream cosFile, String key, String upToken, Region region) throws IOException {
        if (cosFile == null) {
            return null;
        }
        return putRet(buildUploadManager(region, null).put(cosFile, key, upToken, null, null));
    }

    /**
     * 七牛云数据流文件上传
     * 适用于所有的InputStream子类
     * 使用默认upToken、默认的region为华东
     *
     * @param cosFile 需要上传的数据流
     * @param key     上传的路径,默认不指定key的情况下，以文件内容的hash值作为文件名
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     */
    public static DefaultPutRet uploader(InputStream cosFile, String key) throws IOException {
        return uploader(cosFile, key, getUploadToken(), region);
    }

    /**
     * 字符串数据上传
     *
     * @param key         上传的路径,默认不指定key的情况下，以文件内容的hash值作为文件名
     * @param upToken     七牛云上传的token
     * @param region      需要上传到的区域
     * @param str         需要上传的字符串
     * @param charsetName 字符集(如:UTF-8)
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     */
    public static DefaultPutRet stringUploader(String key, String upToken, Region region, String str, String charsetName) throws IOException {
        if (org.apache.commons.lang.StringUtils.isBlank(str)) {
            return null;
        }
        ByteArrayInputStream inputStream = IoUtil.toStream(str, charsetName);
        return uploader(inputStream, key, upToken, region);
    }

    /**
     * 字符串数据上传
     * 使用默认upToken、默认的region为华东
     *
     * @param key         上传的路径,默认不指定key的情况下，以文件内容的hash值作为文件名
     * @param str         需要上传的字符串
     * @param charsetName 字符集(如:UTF-8)
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     */
    public static DefaultPutRet stringUploader(String key, String str, String charsetName) throws IOException {
        return stringUploader(key, getUploadToken(), region, str, charsetName);
    }

    /**
     * 字符串数据上传
     * 使用默认字符集编码(UTF-8)
     * 使用默认upToken、默认的region为华东
     *
     * @param key 上传的路径,默认不指定key的情况下，以文件内容的hash值作为文件名
     * @param str 需要上传的字符串
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     */
    public static DefaultPutRet stringUploader(String key, String str) throws IOException {
        return stringUploader(key, getUploadToken(), region, str, "UTF-8");
    }

    /**
     * 下载远程文本
     *
     * @param url               请求的url
     * @param customCharsetName 自定义的字符集
     * @return 文本
     */
    public static String downloadString(String url, String customCharsetName) {
        return HttpUtil.downloadString(url, customCharsetName);
    }


    /**
     * 七牛云字节数组文件上传
     * 可以支持将内存中的字节数组上传到空间中。
     *
     * @param uploadBytes 需要上传的字节数组文件
     * @param key         上传的路径,默认不指定key的情况下，以文件内容的hash值作为文件名
     * @param upToken     七牛云上传的token
     * @param region      需要上传到的区域
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     */
    public static DefaultPutRet uploader(final byte[] uploadBytes, String key, String upToken, Region region) throws IOException {
        if (uploadBytes == null) {
            return null;
        }
        return putRet(buildUploadManager(region, null).put(uploadBytes, key, upToken));
    }

    /**
     * 七牛云字节数组文件上传
     * 可以支持将内存中的字节数组上传到空间中。
     * 使用默认upToken、默认的region为华东
     *
     * @param uploadBytes 需要上传的字节数组文件
     * @param key         上传的路径,默认不指定key的情况下，以文件内容的hash值作为文件名
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     */
    public static DefaultPutRet uploader(final byte[] uploadBytes, String key) throws IOException {
        return uploader(uploadBytes, key, getUploadToken(), region);
    }

    /**
     * 七牛云异步上传数据
     * 将内存中的字节数组异步上传到空间中
     *
     * @param data     上传的数据
     * @param key      上传数据保存的文件名
     * @param token    上传凭证
     * @param params   自定义参数，如 params.put("x:foo", "foo")
     * @param mime     指定文件mimeType
     * @param checkCrc 是否验证crc32
     * @param handler  上传完成的回调函数
     * @param region   需要上传到的区域
     */
    public static void uploader(final byte[] data, final String key, final String token, StringMap params, String mime,
                                boolean checkCrc, UpCompletionHandler handler, Region region) throws IOException {
        buildUploadManager(region, null).asyncPut(data, key, token, params, mime, checkCrc, handler);
    }

    /**
     * 七牛云异步上传数据
     * 将内存中的字节数组异步上传到空间中
     * 使用默认upToken、默认的region为华东
     *
     * @param data     上传的数据
     * @param key      上传数据保存的文件名
     * @param params   自定义参数，如 params.put("x:foo", "foo")
     * @param mime     指定文件mimeType
     * @param checkCrc 是否验证crc32
     * @param handler  上传完成的回调函数
     */
    public static void uploader(final byte[] data, final String key, StringMap params, String mime,
                                boolean checkCrc, UpCompletionHandler handler) throws IOException {
        uploader(data, key, getUploadToken(), params, mime, checkCrc, handler, region);
    }

    /**
     * 七牛云异步上传数据精简版
     * 将内存中的字节数组异步上传到空间中
     * 取消一些不常用的参数
     *
     * @param data    上传的数据
     * @param key     上传数据保存的文件名
     * @param token   上传凭证
     * @param handler 上传完成的回调函数
     * @param region  需要上传到的区域
     */
    public static void uploader(final byte[] data, final String key, final String token,
                                UpCompletionHandler handler, Region region) throws IOException {
        buildUploadManager(region, null).asyncPut(data, key, token, null, null, false, handler);
    }

    /**
     * 七牛云异步上传数据精简版
     * 将内存中的字节数组异步上传到空间中
     * 取消一些不常用的参数
     * 使用默认upToken、默认的region为华东
     *
     * @param data    上传的数据
     * @param key     上传数据保存的文件名
     * @param handler 上传完成的回调函数
     */
    public static void uploader(final byte[] data, final String key, UpCompletionHandler handler) throws IOException {
        uploader(data, key, getUploadToken(), handler, region);
    }

    /**
     * 获取七牛云文件信息
     *
     * @param key    key值
     * @param region 需要上传到的区域
     * @return 返回null为获取失败, 否则返回com.qiniu.storage.model.FileInfo实体信息
     */
    public static FileInfo getFileInfo(String key, Region region) throws QiniuException {
        return getBucketManager(region).stat(bucket, key);
    }

    /**
     * 获取七牛云文件信息
     * 使用默认upToken、默认的region为华东
     *
     * @param key key值
     * @return 返回null为获取失败, 否则返回com.qiniu.storage.model.FileInfo实体信息
     */
    public static FileInfo getFileInfo(String key) throws QiniuException {
        return getFileInfo(key, region);
    }

    /**
     * 修改文件类型
     *
     * @param key         key值
     * @param region      区域信息
     * @param newMimeType 需要修改的新的文件类型
     * @return 返回定义HTTP请求的信息
     */
    public static Response setFileType(String key, Region region, String newMimeType) throws QiniuException {
        return getBucketManager(region).changeMime(bucket, key, newMimeType);
    }

    /**
     * 修改文件类型
     * 使用默认upToken、默认的region为华东
     *
     * @param key         key值
     * @param newMimeType 需要修改的新的文件类型
     * @return 返回定义HTTP请求的信息
     */
    public static Response setFileType(String key, String newMimeType) throws QiniuException {
        return setFileType(key, region, newMimeType);
    }

    /**
     * 获取私有空间文件链接进行私有授权签名
     *
     * @param publicUrl       待签名文件url，如 http://img.domain.com/u/3.jpg 、
     *                        http://img.domain.com/u/3.jpg?imageView2/1/w/120
     * @param expireInSeconds 有效时长，单位秒。默认3600s
     * @return 返回文件下载签名
     */
    public static String getPrivateDownloadUrl(String publicUrl, long expireInSeconds) {
        return Auth.create(accessKey, secretKey).privateDownloadUrl(publicUrl, expireInSeconds);
    }

    /**
     * 获取空间文件列表
     *
     * @param prefix    文件名前缀
     * @param limit     每次迭代的长度限制，最大1000，推荐值 1000
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @param region    区域信息
     * @return 返回文件列表迭代器(getFilesList.hasNext ())
     */
    public static BucketManager.FileListIterator getFilesList(String prefix, int limit, String delimiter, Region region) {
        BucketManager bucketManager = getBucketManager(region);
        // 列举空间文件列表
        return bucketManager.createFileListIterator(bucket, prefix, limit, delimiter);
    }

    /**
     * 获取空间文件列表
     * 使用默认upToken、默认的region为华东
     *
     * @param prefix    文件名前缀
     * @param limit     每次迭代的长度限制，最大1000，推荐值 1000
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return 返回文件列表迭代器(getFilesList.hasNext ())
     */
    public static BucketManager.FileListIterator getFilesList(String prefix, int limit, String delimiter) {
        return getFilesList(prefix, limit, delimiter, region);
    }

    /**
     * 抓取网络资源到空间
     *
     * @param remoteSrcUrl 远程url、需要抓取的远程url
     * @param key          你的文件的key值
     * @param region       区域信息
     * @return 返回fetch 接口的回复对象
     * 参考文档：<a href="https://developer.qiniu.com/kodo/api/fetch">资源抓取</a>
     */
    public static FetchRet fetch(String remoteSrcUrl, String key, Region region) throws QiniuException {
        return getBucketManager(region).fetch(remoteSrcUrl, bucket, key);
    }

    /**
     * 抓取网络资源到空间
     * 使用默认upToken、默认的region为华东
     *
     * @param remoteSrcUrl 远程url、需要抓取的远程url
     * @param key          你的文件的key值
     * @return 返回fetch 接口的回复对象
     * 参考文档：<a href="https://developer.qiniu.com/kodo/api/fetch">资源抓取</a>
     */
    public static FetchRet fetch(String remoteSrcUrl, String key) throws QiniuException {
        return fetch(remoteSrcUrl, key, region);
    }

    /**
     * 异步第三方资源抓取 从指定 URL 抓取资源，并将该资源存储到指定空间中。每次只抓取一个文件，抓取时可以指定保存空间名和最终资源名。
     * 主要对于大文件进行抓取
     * https://developer.qiniu.com/kodo/api/4097/asynch-fetch
     *
     * @param url    待抓取的文件链接，支持设置多个,以';'分隔
     * @param bucket 文件抓取后保存的空间
     * @param key    文件抓取后保存的文件名
     * @return Response
     */
    public Response asyncFetch(String url, String bucket, String key) throws QiniuException {
        StringMap params = new StringMap().putNotNull("key", key);
        return getBucketManager(region).asyncFetch(url, bucket, params);
    }

    /**
     * 删除空间中的文件
     *
     * @param key    你的文件的key值
     * @param region 区域信息
     * @return 返回定义HTTP请求的信息
     */
    public static Response delete(String key, Region region) throws QiniuException {
        return getBucketManager(region).delete(bucket, key);
    }

    /**
     * 删除空间中的文件
     * 使用默认upToken、默认的region为华东
     *
     * @param key 你的文件的key值
     * @return 返回定义HTTP请求的信息
     */
    public static Response delete(String key) throws QiniuException {
        return delete(key, region);
    }

    /**
     * 批量删除空间中的文件
     *
     * @param keyList 文件key，单次批量请求的文件数量不得超过1000
     * @param region  区域信息
     * @return 返回定义批量请求的状态码
     * 参考文档：<a href="https://developer.qiniu.com/kodo/api/batch">批量操作</a>
     */
    public static BatchStatus[] delete(String[] keyList, Region region) throws QiniuException {
        BucketManager bucketManager = getBucketManager(region);
        BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
        batchOperations.addDeleteOp(bucket, keyList);
        Response response = bucketManager.batch(batchOperations);
        return response.jsonToObject(BatchStatus[].class);
    }

    /**
     * 批量删除空间中的文件
     * 使用默认upToken、默认的region为华东
     *
     * @param keyList 文件key，单次批量请求的文件数量不得超过1000
     * @return 返回定义批量请求的状态码
     * 参考文档：<a href="https://developer.qiniu.com/kodo/api/batch">批量操作</a>
     */
    public static BatchStatus[] delete(String[] keyList) throws QiniuException {
        return delete(keyList, region);
    }

    /**
     * 重命名空间中的文件
     *
     * @param bucket     空间名称
     * @param oldFileKey 旧的文件名称
     * @param newFileKey 新的文件名称
     * @param force      强制覆盖空间中已有同名（和 newFileKey 相同）的文件
     * @return 返回定义HTTP请求的信息
     */
    public static Response rename(String bucket, String oldFileKey, String newFileKey, boolean force) throws QiniuException {
        return getBucketManager(region).rename(bucket, oldFileKey, newFileKey, force);
    }

    /**
     * 重命名空间中的文件
     * 使用默认upToken、默认的region为华东
     *
     * @param oldFileKey 旧的文件名称
     * @param newFileKey 新的文件名称
     * @return 返回定义HTTP请求的信息
     */
    public static Response rename(String oldFileKey, String newFileKey) throws QiniuException {
        return rename(bucket, oldFileKey, newFileKey, false);
    }

    /**
     * 复制文件，要求空间在同一账号下，可以设置force参数为true强行覆盖空间已有同名文件
     *
     * @param fromBucket  源空间名称
     * @param fromFileKey 源文件名称
     * @param toBucket    目的空间名称
     * @param toFileKey   目的文件名称
     * @param force       强制覆盖空间中已有同名（和 toFileKey 相同）的文件
     * @return 返回定义HTTP请求的信息
     */
    public static Response copy(String fromBucket, String fromFileKey, String toBucket, String toFileKey, boolean force) throws QiniuException {
        return getBucketManager(region).copy(fromBucket, fromFileKey, toBucket, toFileKey, force);
    }

    /**
     * 复制文件，要求空间在同一账号下
     * 使用默认upToken、默认的region为华东
     *
     * @param fromFileKey 源文件名称
     * @param toFileKey   目的文件名称
     * @return 返回定义HTTP请求的信息
     */
    public static Response copy(String fromFileKey, String toFileKey) throws QiniuException {
        return copy(bucket, fromFileKey, bucket, toFileKey, false);
    }

    /**
     * 移动文件，要求空间在同一账号下, 可以添加force参数为true强行移动文件。
     *
     * @param fromBucket  源空间名称
     * @param fromFileKey 源文件名称
     * @param toBucket    目的空间名称
     * @param toFileKey   目的文件名称
     * @param force       强制覆盖空间中已有同名（和 toFileKey 相同）的文件
     * @return 返回定义HTTP请求的信息
     */
    public static Response move(String fromBucket, String fromFileKey, String toBucket, String toFileKey, boolean force) throws QiniuException {
        return getBucketManager(region).move(fromBucket, fromFileKey, toBucket, toFileKey, force);
    }

    /**
     * 移动文件。要求空间在同一账号下
     * 使用默认upToken、默认的region为华东
     *
     * @param fromFileKey 源文件名称
     * @param toFileKey   目的文件名称
     * @return 返回定义HTTP请求的信息
     */
    public static Response move(String fromFileKey, String toFileKey) throws QiniuException {
        return move(bucket, fromFileKey, bucket, toFileKey, false);
    }

    /**
     * 一般情况下可通过此方法获取token
     * 有效时长3600秒
     *
     * @return 生成七牛云上传的token
     */
    public static String getUploadToken() {
        return Auth.create(accessKey, secretKey).uploadToken(bucket);
    }

    /**
     * 一般情况下可通过此方法获取token
     * <p>
     * 自定义上传策略
     * 有效时长默认3600s
     *
     * @param policy 上传策略的其它参数，如 new StringMap().put("endUser", "uid").putNotEmpty("returnBody", "")。
     *               scope通过 bucket、key间接设置，deadline 通过 expires 间接设置
     * @return 生成的上传token
     */
    public static String getUploadToken(StringMap policy) {
        return Auth.create(accessKey, secretKey).uploadToken(bucket, null, 3600, policy);
    }

    /**
     * 同名文件覆盖操作、只能上传指定key的文件可以可通过此方法获取token
     * 这个文件名称同时是客户端上传代码中指定的文件名，两者必须一致
     *
     * @param key 七牛云上传路径,可为 null
     * @return 生成的上传token
     */
    public static String getUploadToken(String key) {
        return Auth.create(accessKey, secretKey).uploadToken(bucket, key);
    }

    /**
     * 同名文件覆盖操作、只能上传指定key的文件可以可通过此方法获取token
     * 这个文件名称同时是客户端上传代码中指定的文件名，两者必须一致
     * <p>
     * 自定义上传策略
     * 有效时长默认3600s
     *
     * @param key    七牛云上传路径,可为 null
     * @param policy 上传策略的其它参数，如 new StringMap().put("endUser", "uid").putNotEmpty("returnBody", "")。
     *               scope通过 bucket、key间接设置，deadline 通过 expires 间接设置
     * @return 生成的上传token
     */
    public static String getUploadToken(String key, StringMap policy) {
        return Auth.create(accessKey, secretKey).uploadToken(bucket, key, 3600, policy);
    }

    /**
     * 生成token
     * 指定有效时长
     *
     * @param expires 有效时长，单位秒。默认3600s
     * @return 生成的上传token
     */
    public static String getUploadToken(long expires) {
        return Auth.create(accessKey, secretKey).uploadToken(bucket, null, expires, null, true);
    }

    /**
     * 生成token
     * 指定key、指定有效时长
     * 同名文件覆盖操作、只能上传指定key的文件
     *
     * @param key     七牛云上传唯一标识 ，同名的覆盖，可为null
     * @param expires 有效时长，单位秒。默认3600s
     * @return 生成上传的token
     */
    public static String getUploadToken(String key, long expires) {
        return Auth.create(accessKey, secretKey).uploadToken(bucket, key, expires, null, true);
    }

    /**
     * 生成token
     * 自定义上传策略、指定有效时长
     *
     * @param expires 有效时长，单位秒。默认3600s
     * @param policy  上传策略的其它参数，如 new StringMap().put("endUser", "uid").putNotEmpty("returnBody", "")。
     *                scope通过 bucket、key间接设置，deadline 通过 expires 间接设置
     * @return 生成的上传token
     */
    public static String getUploadToken(long expires, StringMap policy) {
        return Auth.create(accessKey, secretKey).uploadToken(bucket, null, expires, policy, true);
    }

    /**
     * 生成上传token
     *
     * @param key     key，可为 null
     * @param expires 有效时长，单位秒。默认3600s
     * @param policy  上传策略的其它参数，如 new StringMap().put("endUser", "uid").putNotEmpty("returnBody", "")。
     *                scope通过 bucket、key间接设置，deadline 通过 expires 间接设置
     * @param strict  是否去除非限定的策略字段，默认true
     * @return 生成的上传token
     */
    public static String getUploadToken(String key, long expires, StringMap policy, boolean strict) {
        return Auth.create(accessKey, secretKey).uploadToken(bucket, key, expires, policy, strict);
    }

    /**
     * <p>验证回调签名是否正确<p/>
     * <p>具体参考:https://developer.qiniu.com/kodo/1653/callback<p/>
     *
     * @param authorization 待验证签名字符串，以 "QBox "作为起始字符
     * @param url           回调地址
     * @param body          回调请求体。原始请求体，不要解析后再封装成新的请求体--可能导致签名不一致。
     * @param contentType   回调ContentType
     * @return 返回当前回调签名是否正确 true:正确
     */
    public static boolean isValidCallback(String authorization, String url, byte[] body, String contentType) {
        return Auth.create(accessKey, secretKey).isValidCallback(authorization, url, body, contentType);
    }

    /**
     * 计算输入流的etag，如果计算完毕不需要这个InputStream对象，请自行关闭流
     * etag算法是七牛用来标志数据唯一性的算法。
     *
     * @param in  数据输入流
     * @param len 数据流长度
     * @return 数据流的etag值
     */
    public static String getEtag(InputStream in, long len) throws IOException {
        return Etag.stream(in, len);
    }

    /**
     * 计算二进制数据的etag
     * etag算法是七牛用来标志数据唯一性的算法。
     *
     * @param data   二进制数据
     * @param offset 起始字节索引
     * @param length 需要计算的字节长度
     * @return 二进制数据的etag
     */
    public static String getEtag(byte[] data, int offset, int length) {
        return Etag.data(data, offset, length);
    }

    /**
     * 计算二进制数据的etag
     *
     * @param data 二进制数据
     * @return 二进制数据的etag
     */
    public static String getEtag(byte[] data) {
        return Etag.data(data);
    }

    /**
     * 计算文件内容的etag
     *
     * @param file 文件对象
     * @return 文件内容的etag
     */
    public static String getEtag(File file) throws IOException {
        return Etag.file(file);
    }

    /**
     * 计算文件内容的etag
     *
     * @param filePath 文件路径
     * @return 文件内容的etag
     */
    public static String getEtag(String filePath) throws IOException {
        return Etag.file(filePath);
    }
}

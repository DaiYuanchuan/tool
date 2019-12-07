package cn.novelweb.tool.upload.qiniu;

import cn.hutool.core.lang.Singleton;
import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.*;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FetchRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.Etag;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

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
     * 构建七牛云文件上传管理器
     *
     * @param region 需要上传到的区域
     * @return 返回七牛云文件上传管理器<code>UploadManager</code>类
     */
    private static UploadManager buildUploadManager(Region region) {
        // 构造一个带指定Region对象的配置类
        Configuration configuration = Singleton.get(Configuration.class, region);
        // 构造一个带指定Zone对象的配置类
        return Singleton.get(UploadManager.class, configuration);
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
     * @throws QiniuException 抛出七牛云异常
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
     *
     * @param cosFile 需要上传的文件对象
     * @param key     上传文件保存的文件名
     * @param upToken 上传凭证
     * @param region  需要上传到的区域
     * @return 返回null为上传失败, 否则返回默认上传接口回复对象DefaultPutRet
     * @throws QiniuException 抛出七牛云异常
     */
    public static DefaultPutRet uploader(File cosFile, String key, String upToken, Region region) throws QiniuException {
        if (cosFile == null) {
            return null;
        }
        return putRet(buildUploadManager(region).put(cosFile, key, upToken));
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
     * @throws QiniuException 抛出七牛云异常
     */
    public static DefaultPutRet uploader(InputStream cosFile, String key, String upToken, Region region) throws QiniuException {
        if (cosFile == null) {
            return null;
        }
        return putRet(buildUploadManager(region).put(cosFile, key, upToken, null, null));
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
     * @throws QiniuException 抛出七牛云异常
     */
    public static DefaultPutRet uploader(final byte[] uploadBytes, String key, String upToken, Region region) throws QiniuException {
        if (uploadBytes == null) {
            return null;
        }
        return putRet(buildUploadManager(region).put(uploadBytes, key, upToken));
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
     * @throws IOException 抛出IO异常
     */
    public static void uploader(final byte[] data, final String key, final String token, StringMap params,
                                String mime, boolean checkCrc, UpCompletionHandler handler, Region region) throws IOException {
        buildUploadManager(region).asyncPut(data, key, token, params, mime, checkCrc, handler);
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
     * @throws IOException 抛出IO异常
     */
    public static void uploader(final byte[] data, final String key, final String token,
                                UpCompletionHandler handler, Region region) throws IOException {
        buildUploadManager(region).asyncPut(data, key, token, null, null, false, handler);
    }

    /**
     * 获取七牛云文件信息
     *
     * @param key    key值
     * @param region 需要上传到的区域
     * @return 返回null为获取失败, 否则返回com.qiniu.storage.model.FileInfo实体信息
     * @throws QiniuException 抛出七牛云异常
     */
    public static FileInfo getFileInfo(String key, Region region) throws QiniuException {
        return getBucketManager(region).stat(bucket, key);
    }

    /**
     * 修改文件类型
     *
     * @param key         key值
     * @param region      区域信息
     * @param newMimeType 需要修改的新的文件类型
     * @return 返回定义HTTP请求的信息
     * @throws QiniuException 抛出七牛云异常
     */
    public static Response setFileType(String key, Region region, String newMimeType) throws QiniuException {
        return getBucketManager(region).changeMime(bucket, key, newMimeType);
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
     * 抓取网络资源到空间
     *
     * @param remoteSrcUrl 远程url、需要抓取的远程url
     * @param key          你的文件的key值
     * @param region       区域信息
     * @return 返回fetch 接口的回复对象
     * 参考文档：<a href="https://developer.qiniu.com/kodo/api/fetch">资源抓取</a>
     * @throws QiniuException 抛出七牛云异常
     */
    public static FetchRet fetch(String remoteSrcUrl, String key, Region region) throws QiniuException {
        return getBucketManager(region).fetch(remoteSrcUrl, bucket, key);
    }

    /**
     * 删除空间中的文件
     *
     * @param key    你的文件的key值
     * @param region 区域信息
     * @return 返回定义HTTP请求的信息
     * @throws QiniuException 抛出七牛云异常
     */
    public static Response delete(String key, Region region) throws QiniuException {
        return getBucketManager(region).delete(bucket, key);
    }

    /**
     * 批量删除空间中的文件
     *
     * @param keyList 文件key，单次批量请求的文件数量不得超过1000
     * @param region  区域信息
     * @return 返回定义批量请求的状态码
     * 参考文档：<a href="https://developer.qiniu.com/kodo/api/batch">批量操作</a>
     * @throws QiniuException 抛出七牛云异常
     */
    public static BatchStatus[] delete(String[] keyList, Region region) throws QiniuException {
        BucketManager bucketManager = getBucketManager(region);
        BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
        batchOperations.addDeleteOp(bucket, keyList);
        Response response = bucketManager.batch(batchOperations);
        return response.jsonToObject(BatchStatus[].class);
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
     * 生成token
     * 指定有效时长
     *
     * @param expires 有效时长，单位秒。默认3600s
     * @return 生成的上传token
     */
    public static String getUploadToken(long expires) {
        return Auth.create(accessKey, secretKey)
                .uploadToken(bucket, null, expires, null, true);
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
        return Auth.create(accessKey, secretKey)
                .uploadToken(bucket, key, expires, null, true);
    }

    /**
     * 计算输入流的etag，如果计算完毕不需要这个InputStream对象，请自行关闭流
     * etag算法是七牛用来标志数据唯一性的算法。
     *
     * @param in  数据输入流
     * @param len 数据流长度
     * @return 数据流的etag值
     * @throws IOException 文件读取异常
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
     * @throws IOException 文件读取异常
     */
    public static String getEtag(File file) throws IOException {
        return Etag.file(file);
    }

    /**
     * 计算文件内容的etag
     *
     * @param filePath 文件路径
     * @return 文件内容的etag
     * @throws IOException 文件读取异常
     */
    public static String getEtag(String filePath) throws IOException {
        return Etag.file(filePath);
    }

}

package cn.novelweb.tool.upload.qcloud;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.novelweb.tool.upload.qcloud.callback.ListObjectCompletionHandler;
import com.alibaba.fastjson.JSONObject;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import com.qcloud.cos.auth.COSCredentials;
import lombok.extern.slf4j.Slf4j;
import com.qcloud.cos.region.Region;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.*;

/**
 * <p>腾讯云上传</p>
 * <p>2019-11-23 15:47</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class CosUpload {

    /**
     * 腾讯云secretId信息
     */
    public static String secretId;

    /**
     * 腾讯云secretKey信息
     */
    public static String secretKey;

    /**
     * 当前密钥的appId信息
     */
    public static String appId;

    /**
     * 存储桶的名称 如 qcloud-1256166828
     * 在存储桶列表中的所属地域查看
     * 存储桶名称格式：BucketName-APPID
     */
    public static String bucket;

    /**
     * cos 地区 regionName = new com.qcloud.cos.region.Region("ap-shanghai");
     * 可用区域简称 Region 参考下表
     * 地域                地域简称
     * 北京一区(华北)       ap-beijing-1
     * 北京                ap-beijing
     * 上海(华东)          ap-shanghai
     * 广州(华南)          ap-guangzhou
     * 成都(西南)          ap-chengdu
     * 重庆	              ap-chongqing
     * 香港	              ap-hongkong
     * 新加坡             ap-singapore
     * 多伦多             na-toronto
     * 法兰克福           eu-frankfurt
     * 孟买               ap-mumbai
     * 首尔               ap-seoul
     * 硅谷               na-siliconvalley
     * 弗吉尼亚           na-ashburn
     * 曼谷               ap-bangkok
     * 莫斯科             eu-moscow
     */
    public static Region regionName;

    /**
     * 使用临时密钥时需要指定
     * 临时密钥允许操作的权限列表
     * 简单上传、表单上传和分片上传需要以下的权限
     * 其他权限参考 //cloud.tencent.com/document/product/436/31923
     */
    public static String[] allowActions = new String[]{
            // 简单上传
            "name/cos:PutObject",
            // 表单上传、小程序上传
            "name/cos:PostObject",
            // 分片上传
            "name/cos:InitiateMultipartUpload",
            "name/cos:ListMultipartUploads",
            "name/cos:ListParts",
            "name/cos:UploadPart",
            "name/cos:CompleteMultipartUpload"
    };

    /**
     * 使用 永久密钥 初始化 COSClient
     *
     * @return 返回 永久密钥 COSClient 对象
     */
    public static COSClient initCosClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(regionName);
        return new COSClient(cred, clientConfig);
    }

    /**
     * 使用自定义的 ClientConfig 类
     * 使用 永久密钥 初始化 COSClient
     *
     * @param clientConfig 自定义的 ClientConfig 类 参考:https://cloud.tencent.com/document/product/436/10199
     * @return 返回 永久密钥 COSClient 对象
     */
    public static COSClient initCosClient(ClientConfig clientConfig) {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        return new COSClient(cred, clientConfig);
    }

    /**
     * 创建存储桶
     * 用户确定地域和存储桶名称后，即可创建存储桶
     * 创建存储桶需要使用永久密钥初始化的COSClient
     *
     * @param regionName  cos 地区,定义你要在哪创建这个存储桶 "ap-shanghai"
     * @param bucket      需要创建的存储桶名称,格式：BucketName-APPID
     * @param controlList 存储桶 bucket 的权限 PublicRead(公有读私有写), 其他可选有私有读写, 公有读写
     * @return 返回存储桶对象
     */
    public static Bucket createBucket(String regionName, String bucket, CannedAccessControlList controlList) {
        // 获取COS客户端实例
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        COSClient cosClient = new COSClient(cred, clientConfig);
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucket);
        createBucketRequest.setCannedAcl(controlList);
        return cosClient.createBucket(createBucketRequest);
    }

    /**
     * 查询存储桶列表
     * 查询用户的存储桶列表
     *
     * @return 存储桶列表
     */
    public static List<Bucket> getBucketList() {
        COSClient cosClient = initCosClient();
        return cosClient.listBuckets();
    }

    /**
     * 查询存储桶列表
     * 查询用户的存储桶列表
     * 使用自定义的cos客户端
     *
     * @param cosClient 自定义的cos客户端
     * @return 存储桶列表
     */
    public static List<Bucket> getBucketList(COSClient cosClient) {
        return cosClient.listBuckets();
    }

    /**
     * 创建目录
     * 创建目录 实际上就是创建一个 以 / 为结尾的空文件
     *
     * @param cosClient cos 客户端
     * @param key       目录的名称
     * @return 返回创建目录的请求结果
     */
    public static PutObjectResult mkdir(COSClient cosClient, String key) {
        // 目录对象即是一个/结尾的空文件，上传一个长度为 0 的 byte 流
        InputStream input = new ByteArrayInputStream(new byte[0]);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, input, objectMetadata);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 简单的将本地文件上传到COS
     * 适用于20M以下图片类小文件上传，最大支持上传不超过5GB文件
     * 若 COS 上已存在同样 Key 的对象，上传时则会覆盖旧的对象。
     *
     * @param cosClient 构建的cos客户端
     * @param file      需要上传的本地文件(小于20M以下)
     * @param key       对象键（Key）是对象在存储桶中的唯一标识
     * @return 返回简单上传的请求结果
     */
    public static PutObjectResult uploader(COSClient cosClient, File file, String key) {
        return cosClient.putObject(bucket, key, file);
    }

    /**
     * 简单的从输入流上传(需提前告知输入流的长度, 否则可能导致 oom)
     * 适用于20M以下图片类小文件上传，最大支持上传不超过5GB文件
     * 若 COS 上已存在同样 Key 的对象，上传时则会覆盖旧的对象。
     *
     * @param cosClient   构建的cos客户端
     * @param cosFile     数据流
     * @param key         对象键（Key）是对象在存储桶中的唯一标识
     * @param contentType 设置 Content type, 默认是 application/octet-stream
     * @return 返回简单上传的请求结果
     */
    public static PutObjectResult uploader(COSClient cosClient, InputStream cosFile,
                                           String key, String contentType) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        try {
            objectMetadata.setContentLength(cosFile.available());
            objectMetadata.setContentType(contentType);
            return cosClient.putObject(bucket, key, cosFile, objectMetadata);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 上传对象
     * 上传接口根据用户文件的长度，自动选择简单上传以及分块上传
     * 用户不用关心分块上传的每个步骤。
     *
     * @param cosClient  构建的cos客户端
     * @param file       需要上传的本地文件
     * @param key        对象键（Key）是对象在存储桶中的唯一标识
     * @param threadSize 需要构建的线程池大小
     *                   建议在客户端与 COS 网络充足的情况下，设置成16或32即可
     *                   对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时
     * @return 返回 Upload 实例，可以查询上传是否结束，也可同步的等待上传结束。
     * 如果想同步的等待上传结束，则调用 upload.waitForCompletion() 方法
     */
    public static Upload uploader(COSClient cosClient, File file, String key, int threadSize) {
        ExecutorService threadPool = ThreadUtil.newExecutor(threadSize);
        TransferManager transferManager = new TransferManager(cosClient, threadPool);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
        return transferManager.upload(putObjectRequest);
    }

    /**
     * 下载对象
     * 下载对象到本地
     *
     * @param cosClient 构建的cos客户端
     * @param file      需要写入到本地的文件
     * @param key       对象键,需要进行下载的对象key值
     * @return 返回文件的属性 ObjectMetadata，包含文件的自定义头和 content-type 等属性。
     */
    public static ObjectMetadata download(COSClient cosClient, File file, String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);
        return cosClient.getObject(getObjectRequest, file);
    }

    /**
     * 删除单个指定的对象
     *
     * @param cosClient 构建的cos客户端
     * @param key       对象键,你需要删除的对象key值
     * @return 返回删除状态:true 删除成功 false:删除失败
     */
    public static boolean delete(COSClient cosClient, String key) {
        return delete(cosClient, key, bucket);
    }

    /**
     * 删除单个指定的对象
     * 指定删除存储桶下指定的对象
     *
     * @param cosClient  构建的 cos 客户端
     * @param key        对象键,你需要删除的对象key值
     * @param bucketName 指定存储桶的名称,Bucket 的命名格式为 BucketName-APPID
     * @return 返回删除状态:true 删除成功 false:删除失败
     */
    public static boolean delete(COSClient cosClient, String key, String bucketName) {
        try {
            cosClient.deleteObject(bucketName, key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 指定存储桶删除多个对象
     * 删除多个指定的对象
     *
     * @param cosClient  构建的cos客户端
     * @param bucketName 指定存储桶的名称,Bucket 的命名格式为 BucketName-APPID
     * @param key        对象键数组,你需要删除的对象key值列表,最多一次删除1000个
     * @return 返回删除状态:true 删除成功 false:删除失败
     */
    public static boolean delete(COSClient cosClient, String bucketName, String... key) {
        if (ArrayUtil.isEmpty(key)) {
            return false;
        }
        // 设置要删除的key列表, 最多一次删除1000个
        List<DeleteObjectsRequest.KeyVersion> keyList = new ArrayList<>();
        for (String str : key) {
            keyList.add(new DeleteObjectsRequest.KeyVersion(str));
        }
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
        deleteObjectsRequest.setKeys(keyList);

        // 批量删除文件
        try {
            cosClient.deleteObjects(deleteObjectsRequest);
            return true;
        } catch (MultiObjectDeleteException mde) {
            log.error("部分删除成功部分失败:{}", JSONObject.toJSON(mde.getErrors()));
            log.error("已经删除的key:{}", mde.getDeletedObjects());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除多个对象
     * 删除多个指定的对象
     *
     * @param cosClient 构建的cos客户端
     * @param key       对象键数组,你需要删除的对象key值列表,最多一次删除1000个
     * @return 返回删除状态:true 删除成功 false:删除失败
     */
    public static boolean delete(COSClient cosClient, String... key) {
        return delete(cosClient, bucket, key);
    }


    /**
     * 查询存储桶中的文件列表
     * 查询存储桶下的部分或者全部对象
     *
     * @param cosClient COS客户端
     * @param prefix    你需要列出的目录前缀
     *                  如:images/ 表示列出的object的key以images/开始
     * @param delimiter 表示分隔符,设置为/表示列出当前目录下的object,设置为空表示列出所有的object
     * @param maxKeys   设置最大遍历出多少个对象,一次listObject最大支持1000
     * @param handler   获取完成后的回调接口
     */
    public static void getFilesList(COSClient cosClient, String prefix,
                                    String delimiter, int maxKeys,
                                    ListObjectCompletionHandler handler) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucket);
        listObjectsRequest.setPrefix(prefix);
        listObjectsRequest.setDelimiter(delimiter);
        listObjectsRequest.setMaxKeys(maxKeys);
        ObjectListing objectListing;

        try {
            do {
                objectListing = cosClient.listObjects(listObjectsRequest);
                List<String> commonPrefixes = objectListing.getCommonPrefixes();
                List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();
                String nextMarker = objectListing.getNextMarker();
                listObjectsRequest.setMarker(nextMarker);
                handler.complete(commonPrefixes, cosObjectSummaries);
            } while (objectListing.isTruncated());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

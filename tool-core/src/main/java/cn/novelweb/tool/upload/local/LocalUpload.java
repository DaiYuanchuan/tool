package cn.novelweb.tool.upload.local;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.novelweb.tool.upload.file.FileInfo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.novelweb.tool.http.Result;
import cn.novelweb.tool.upload.local.pojo.UploadFileParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>本地文件上传工具类</p>
 * 2019-10-28 14:19
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class LocalUpload {

    /**
     * 默认的文件上传路径(打包后的 jar包 同级目录)
     */
    public static String defaultPath = String.format("log%suploader", File.separatorChar);

    /**
     * 秒传、断点的文件MD5验证
     * 根据文件路径获取要上传的文件夹下的 文件名.conf 文件
     * 通过判断 *.conf 文件状态来验证(有条件的可以使用redis来记录上传状态和文件地址)
     *
     * @param fileMd5      文件的MD5
     * @param fileName     文件名(包含文件格式)
     * @param confFilePath 分片配置文件全路径(不包含文件名)
     * @param tmpFilePath  上传的缓存文件全路径(不包含文件名)
     * @return 返回文件MD5验证状态
     * 200:文件已存在、文件已上传成功、可以执行秒传
     * 206:文件已经上传了一部分、上传了部分分片文件、未上传完整的文件
     * 404:文件不存在、文件没有被上传过、第一次上传
     * @throws Exception 抛出自定义Exception异常
     */
    public static Result<JSONArray> checkFileMd5(String fileMd5,
                                         String fileName,
                                         String confFilePath,
                                         String tmpFilePath) throws Exception {
        boolean isParamEmpty = StringUtils.isBlank(fileMd5)
                || StringUtils.isBlank(fileName)
                || StringUtils.isBlank(confFilePath)
                || StringUtils.isBlank(tmpFilePath);
        if (isParamEmpty) {
            throw new Exception("参数值为空");
        }
        // 构建分片配置文件对象
        File confFile = new File(confFilePath + File.separatorChar + fileName + ".conf");

        // 布尔值:上传的文件缓存对象是否存在
        boolean isTmpFileEmpty = new File(tmpFilePath
                + File.separatorChar + fileName + "_tmp").exists();

        // 分片记录文件 和 文件缓存文件 同时存在 则 状态码定义为 206
        if (confFile.exists() && isTmpFileEmpty) {
            byte[] completeList = FileUtils.readFileToByteArray(confFile);
            List<String> missChunkList = new LinkedList<String>();
            for (int i = 0; i < completeList.length; i++) {
                if (completeList[i] != Byte.MAX_VALUE) {
                    missChunkList.add(Integer.toString(i));
                }
            }
            JSONArray jsonArray = JSON.parseArray(JSONObject.toJSONString(missChunkList));

            return Result.ok(HttpStatus.PARTIAL_CONTENT.value(), "文件已经上传了一部分",
                    jsonArray);
        }

        // 布尔值:上传的文件对象是否存在
        boolean isFileEmpty = new File(tmpFilePath + File.separatorChar + fileName)
                .exists();

        // 上传的文件 和 配置文件 同时存在 则 当前状态码为 200
        if (isFileEmpty && confFile.exists()) {
            return Result.ok(HttpStatus.OK.value(), "文件已上传成功");
        }
        return Result.ok(HttpStatus.NOT_FOUND.value(), "文件不存在");
    }

    /**
     * 使用默认上传路径验证秒传、断点文件配置(/log/uploader/文件的MD5/*)
     *
     * @param fileMd5  文件的MD5
     * @param fileName 文件名(包含文件格式)
     * @return 返回文件MD5验证状态
     * 200:文件已存在、文件已上传成功、可以执行秒传
     * 206:文件已经上传了一部分、上传了部分分片文件、未上传完整的文件
     * 404:文件不存在、文件没有被上传过、第一次上传
     * @throws Exception 抛出自定义Exception异常
     */
    public static Result<JSONArray> checkFileMd5(String fileMd5,
                                         String fileName) throws Exception {
        return checkFileMd5(fileMd5, fileName,
                defaultPath + File.separatorChar + fileMd5
                , defaultPath + File.separatorChar + fileMd5);
    }

    /**
     * 文件分片、断点续传上传程序
     * 创建 文件名.conf 文件记录已上传分片信息
     * 使用 RandomAccessFile(随机访问文件) 类随机指定位置写入文件,类似于合成分片
     * 检验分片文件是否全部上传完成，重命名缓存文件
     *
     * @param param        上传文件时 需要接收的基本参数信息
     * @param confFilePath 分片配置文件的路径,考虑到配置文件与缓存文件分开的情况(不包含文件名)
     * @param filePath     上传文件的路径,同时也是生成缓存文件的路径(不包含文件名)
     * @param chunkSize    每块分片的大小,单位:字节(这个值需要与前端JS的值保持一致) 5M=5242880
     * @param request      HTTP Servlet请求
     * @return 返回文件上传状态
     * 200:文件上传成功、单个分片文件上传成功、未全部上传成功
     * 201:文件全部上传完成、所有分片全部上传完成、文件完成合并后重命名操作(同时返回cn.novelweb.tool.upload.file.FileInfo信息)
     * 500:文件上传失败、文件上传异常
     * @throws Exception 抛出自定义Exception异常
     */
    public static synchronized Result<FileInfo> fragmentFileUploader(UploadFileParam param, String confFilePath,
                                                                     String filePath, long chunkSize,
                                                                     HttpServletRequest request) throws Exception {
        boolean isParamEmpty = StringUtils.isBlank(filePath)
                || StringUtils.isBlank(confFilePath) && param.getFile() == null;
        if (isParamEmpty) {
            throw new Exception("参数值为空");
        }
        // 判断enctype属性是否为multipart/form-data
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            throw new IllegalArgumentException("上传内容不是有效的multipart/form-data类型.");
        }

        try {
            // 分片配置文件
            File confFile = FileUtil.file(FileUtil.mkdir(confFilePath), String.format("%s.conf", param.getName()));
            RandomAccessFile accessConfFile = new RandomAccessFile(confFile, "rw");
            // 把该分段标记为 true 表示完成
            accessConfFile.setLength(param.getChunks());
            accessConfFile.seek(param.getChunk());
            accessConfFile.write(Byte.MAX_VALUE);
            accessConfFile.close();
            // _tmp的缓存文件对象
            File tmpFile = FileUtil.file(FileUtil.mkdir(filePath), String.format("%s_tmp", param.getName()));
            // 随机位置写入文件
            RandomAccessFile accessTmpFile = new RandomAccessFile(tmpFile, "rw");
            long offset = chunkSize * param.getChunk();
            // 定位到该分片的偏移量、写入该分片数据、释放
            accessTmpFile.seek(offset);
            accessTmpFile.write(param.getFile().getBytes());
            accessTmpFile.close();
            // 检查是否全部分片都成功上传
            byte[] completeList = FileUtils.readFileToByteArray(confFile);
            byte isComplete = Byte.MAX_VALUE;
            for (int i = 0; i < completeList.length && isComplete == Byte.MAX_VALUE; i++) {
                // 与运算, 如果有部分没有完成则 isComplete 不是 Byte.MAX_VALUE
                isComplete = (byte) (isComplete & completeList[i]);
            }

            if (isComplete != Byte.MAX_VALUE) {
                return Result.ok(HttpStatus.OK.value(), "文件上传成功");
            }

            boolean isSuccess = renameFile(tmpFile, param.getName());
            if (!isSuccess) {
                throw new Exception("文件重命名时失败");
            }
            // 全部上传成功后构建文件对象
            FileInfo fileInfo = FileInfo.builder()
                    .hash(param.getMd5())
                    .name(param.getName())
                    .type(param.getFile().getContentType())
                    .path(tmpFile.getParent() + File.separatorChar + param.getName())
                    .createTime(System.currentTimeMillis())
                    .build();
            return Result.ok(HttpStatus.CREATED.value(), "文件上传完成", fileInfo);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败");
        }
    }

    /**
     * 使用默认上传路径上传文件分片(/log/uploader/文件的MD5/*)
     *
     * @param param     上传文件时 需要接收的基本参数信息
     * @param chunkSize 每块分片的大小,单位:字节(这个值需要与前端JS的值保持一致) 5M=5242880
     * @param request   HTTP Servlet请求
     * @return 返回文件上传状态
     * 200:文件上传成功、单个分片文件上传成功、未全部上传成功
     * 201:文件全部上传完成、所有分片全部上传完成、文件完成合并后重命名操作(同时返回com.dai.pojo.file.Files信息)
     * 500:文件上传失败、文件上传异常
     * @throws Exception 抛出自定义Exception异常
     */
    public static Result<FileInfo> fragmentFileUploader(UploadFileParam param, long chunkSize,
                                                 HttpServletRequest request) throws Exception {
        return fragmentFileUploader(param, defaultPath + File.separatorChar + param.getMd5(),
                defaultPath + File.separatorChar + param.getMd5(),
                chunkSize, request);
    }

    /**
     * 普通的文件上传程序、不使用分片、断点续传
     *
     * @param param    上传文件时 需要接收的基本参数信息
     * @param filePath 上传文件的路径,不包含文件名 log/uploader
     * @return 返回文件上传状态和com.dai.pojo.file.Files信息
     * 201:文件上传完成
     * 500:文件上传失败、传输异常
     * @throws Exception 抛出自定义Exception异常
     */
    public static Result<FileInfo> regularFileUploader(UploadFileParam param, String filePath) throws Exception {
        boolean isParamEmpty = StringUtils.isBlank(filePath)
                || StringUtils.isBlank(param.getName()) && param.getFile() == null;
        if (isParamEmpty) {
            throw new Exception("参数值为空");
        }

        // 上传的文件夹
        File uploadFolder = new File(filePath);
        // 创建文件夹
        if (!uploadFolder.exists() && !uploadFolder.mkdirs()) {
            return Result.fail(HttpStatus.FORBIDDEN.value(), "上传所需文件夹创建失败");
        }

        // 上传的文件
        File uploadFile = new File(filePath + File.separatorChar + param.getName());

        // 写入文件
        param.getFile().transferTo(uploadFile);

        // 校验文件是否上传成功
        if (uploadFile.length() != param.getFile().getSize()) {
            return Result.error("文件上传失败");
        }

        // 上传成功后构建文件对象
        FileInfo fileInfo = FileInfo.builder()
                .hash(param.getMd5())
                .name(param.getName())
                .type(param.getFile().getContentType())
                .path(uploadFile.getPath())
                .createTime(System.currentTimeMillis())
                .build();
        return Result.ok(HttpStatus.CREATED.value(), "文件上传完成", fileInfo);
    }

    /**
     * 普通的文件上传程序、使用默认上传路径(/log/uploader/年/月/日/当前时间毫秒数.mp4)
     *
     * @param param 上传文件时 需要接收的基本参数信息
     * @return 返回文件上传状态和com.dai.pojo.file.Files信息
     * 201:文件上传完成
     * 500:文件上传失败、传输异常
     * @throws Exception 抛出自定义Exception异常
     */
    public static Result<FileInfo> regularFileUploader(UploadFileParam param) throws Exception {
        boolean isParamEmpty = StringUtils.isBlank(param.getName()) && param.getFile() == null;
        if (isParamEmpty) {
            throw new Exception("参数值为空");
        }

        // 重构文件名
        param.setName(System.currentTimeMillis() + "." + FileTypeUtil.getType(param.getFile().getInputStream()));

        // 重构文件路径
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                String.format("yyyy%sMM%sdd", File.separatorChar, File.separatorChar));

        return regularFileUploader(param, defaultPath + File.separatorChar
                + simpleDateFormat.format(System.currentTimeMillis()));
    }

    /**
     * 用于上传成功后重命名文件
     *
     * @param toBeRenamed   需要重命名的文件对象
     * @param toFileNewName 文件新的名字
     * @return 返回重命名是否成功
     */
    private static boolean renameFile(File toBeRenamed, String toFileNewName) {
        // 检查要重命名的文件是否存在，是否是文件
        if (!toBeRenamed.exists() || toBeRenamed.isDirectory()) {
            return false;
        }
        File newFile = new File(toBeRenamed.getParent()
                + File.separatorChar + toFileNewName);
        // 修改文件名
        return toBeRenamed.renameTo(newFile);
    }
}

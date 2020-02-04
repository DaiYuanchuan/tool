package cn.novelweb.tool.io;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <p></p>
 * <p>2019-12-07 18:19</p>
 *
 * @author Dai Yuanchuan
 **/
public class FileUtils {

    /**
     * 将文件转换成byte字节数组
     *
     * @param filePath 需要转换的文件路径
     * @return 返回字节数组
     */
    public static byte[] toByteArray(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将文件转成byte字节数组
     *
     * @param file 需要转换的文件
     * @return 返回字节数组
     */
    public static byte[] toByteArray(java.io.File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将String字符串写入文件
     *
     * @param filePath 需要写入的文件路径
     * @param content  需要写入的内容
     * @param isAppend 是否需要追加写入
     * @return 返回java.io.File
     */
    public static File write(String filePath, String content, boolean isAppend) {
        FileWriter writer = new FileWriter(filePath);
        return writer.write(content, isAppend);
    }

    /**
     * 将String字符串写入文件
     *
     * @param file     需要写入的文件对象
     * @param content  需要写入的内容
     * @param isAppend 是否需要追加写入
     * @return 返回java.io.File
     */
    public static File write(File file, String content, boolean isAppend) {
        FileWriter writer = new FileWriter(file);
        return writer.write(content, isAppend);
    }

    /**
     * 将String字符串写入文件
     * 默认 覆盖写入
     *
     * @param filePath 需要写入的文件路径
     * @param content  需要写入的内容
     * @return 返回java.io.File
     */
    public static File write(String filePath, String content) {
        FileWriter writer = new FileWriter(filePath);
        return writer.write(content);
    }

    /**
     * 将String字符串写入文件
     * 默认 覆盖写入
     *
     * @param file    需要写入的文件对象
     * @param content 需要写入的内容
     * @return 返回java.io.file
     */
    public static File write(File file, String content) {
        FileWriter writer = new FileWriter(file);
        return writer.write(content);
    }

    /**
     * 将input流转为String字符串
     * 默认使用UTF-8字符集
     *
     * @param inputStream 需要转换的input流
     * @return 返回String字符串
     */
    public static String inputStreamToString(InputStream inputStream) {
        return inputStreamToString(inputStream, "UTF-8");
    }

    /**
     * 将input流转为String字符串
     *
     * @param inputStream 需要转换的input流
     * @param charsetName 字符集名称[默认:UTF-8]
     * @return 返回String字符串
     */
    public static String inputStreamToString(InputStream inputStream, String charsetName) {
        java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            if (StringUtils.isBlank(charsetName)) {
                return result.toString("UTF-8");
            }
            return result.toString(charsetName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 是否为Windows环境
     *
     * @return 是否为Windows环境
     */
    public static boolean isWindows() {
        return FileUtil.isWindows();
    }

}

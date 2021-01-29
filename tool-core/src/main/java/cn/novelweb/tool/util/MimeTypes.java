package cn.novelweb.tool.util;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * <p>用于根据文件确定文件的mimeType的实用工具类</p>
 * <p>2020-12-20 13:02</p>
 *
 * @author Dan
 **/
@Slf4j
public class MimeTypes {

    /**
     * The default MIME type
     */
    private static final String DEFAULT_MIMETYPE = "application/octet-stream";

    /**
     * 当前实例[单例]
     */
    private static MimeTypes mimetypes = null;

    /**
     * 对mime类型的映射
     */
    private final HashMap<String, String> extensionToMimetypeMap = new HashMap<>();

    /**
     * 禁止实例化
     */
    private MimeTypes() {
    }

    /**
     * 获取 MimeTypes 实例
     */
    @SneakyThrows
    public synchronized static MimeTypes getInstance() {
        if (mimetypes != null) {
            return mimetypes;
        }

        mimetypes = new MimeTypes();
        try (InputStream is = mimetypes.getClass().getResourceAsStream("/mime.types")) {
            if (is != null) {
                // 加载文件 mime.types
                log.debug("Loading mime types from file in the classpath: mime.types");
                mimetypes.loadMimeTypes(is);
            } else {
                // 找不到文件 mime.types
                log.warn("Unable to find 'mime.types' file in classpath");
            }
        }

        return mimetypes;
    }

    /**
     * 自定义需要加载的 mime.types
     *
     * @param is 包含有mime类型的流
     */
    @SneakyThrows
    public void loadMimeTypes(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            // 忽略注释和空行
            if (line.startsWith("#") || line.length() == 0) {
                continue;
            }

            StringTokenizer st = new StringTokenizer(line, " \t");
            if (st.countTokens() <= 1) {
                continue;
            }

            String extension = st.nextToken();
            if (st.hasMoreTokens()) {
                String mimetype = st.nextToken();
                extensionToMimetypeMap.put(extension.toLowerCase(), mimetype);
            }
        }
    }

    /**
     * 根据文件名获取文件 mime 类型
     *
     * @param fileName 文件名
     * @return 返回文件对应的mime类型
     */
    public String getMimeTypes(String fileName) {
        int lastPeriodIndex = fileName.lastIndexOf(".");
        if (lastPeriodIndex > 0 && lastPeriodIndex + 1 < fileName.length()) {
            String ext = fileName.substring(lastPeriodIndex + 1).toLowerCase();
            if (extensionToMimetypeMap.containsKey(ext)) {
                return extensionToMimetypeMap.get(ext);
            }
        }

        return DEFAULT_MIMETYPE;
    }

    /**
     * 获取当前文件对应的 mime 类型
     *
     * @param file {@link File} 需要获取的文件
     * @return 返回文件对应的mime类型
     */
    public String getMimeTypes(File file) {
        if (!file.exists()) {
            return DEFAULT_MIMETYPE;
        }

        // 获取当前文件的扩展名
        String ext = FileTypeUtil.getType(file);
        if (StrUtil.isBlank(ext)) {
            return DEFAULT_MIMETYPE;
        }

        if (extensionToMimetypeMap.containsKey(ext.toLowerCase())) {
            return extensionToMimetypeMap.get(ext.toLowerCase());
        }

        return DEFAULT_MIMETYPE;
    }
}

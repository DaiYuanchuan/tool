package cn.novelweb.tool.upload.fastdfs.config;

import cn.novelweb.tool.upload.fastdfs.FastDfsClient;
import cn.novelweb.tool.upload.fastdfs.utils.IniFileReader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * <p>FastDFS配置类</p>
 * <p>2020-02-03 17:36</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Slf4j
public class FastDfsConfig {

    public static final FastDfsConfig DFS_CONFIG = new FastDfsConfig();

    /**
     * 需要读取的配置
     */
    private static final String FAST_DFS_SO_TIMEOUT = "fastdfs.soTimeout";
    private static final String FAST_DFS_CONNECT_TIMEOUT = "fastdfs.connectTimeout";
    private static final String FAST_DFS_MAX_TOTAL = "fastdfs.maxTotal";
    private static final String FAST_DFS_MAX_TOTAL_PER_KEY = "fastdfs.maxTotalPerKey";
    private static final String FAST_DFS_MAX_IDLE_PER_KEY = "fastdfs.maxIdlePerKey";
    private static final String FAST_DFS_TRACKERS = "fastdfs.tracker_servers";

    /**
     * 设置debug日志是否显示
     */
    private static final String FAST_DFS_DEBUG_LOG = "fastdfs.debug_log";

    /**
     * 默认配置
     */
    private static final int DEFAULT_SO_TIMEOUT = 10000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_MAX_TOTAL = 200;
    private static final int DEFAULT_MAX_TOTAL_PER_KEY = 200;
    private static final int DEFAULT_MAX_IDLE_PER_KEY = 50;

    /**
     * 赋值
     */
    private int soTimeout = DEFAULT_SO_TIMEOUT;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int maxTotal = DEFAULT_MAX_TOTAL;
    private int maxTotalPerKey = DEFAULT_MAX_TOTAL_PER_KEY;
    private int maxIdlePerKey = DEFAULT_MAX_IDLE_PER_KEY;

    private Boolean debugLog;
    private String trackerServers;

    public static void init(String confFileName) {
        if (StringUtils.isBlank(confFileName)) {
            log.error("配置文件不存在");
            return;
        }
        IniFileReader iniReader = new IniFileReader(confFileName);
        try {
            DFS_CONFIG.soTimeout = iniReader.getIntValue(FAST_DFS_SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
            DFS_CONFIG.connectTimeout = iniReader.getIntValue(FAST_DFS_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
            DFS_CONFIG.maxTotal = iniReader.getIntValue(FAST_DFS_MAX_TOTAL, DEFAULT_MAX_TOTAL);
            DFS_CONFIG.maxTotalPerKey = iniReader.getIntValue(FAST_DFS_MAX_TOTAL_PER_KEY, DEFAULT_MAX_TOTAL_PER_KEY);
            DFS_CONFIG.maxIdlePerKey = iniReader.getIntValue(FAST_DFS_MAX_IDLE_PER_KEY, DEFAULT_MAX_IDLE_PER_KEY);
            DFS_CONFIG.debugLog = iniReader.getBoolValue(FAST_DFS_DEBUG_LOG,true);
            DFS_CONFIG.trackerServers = iniReader.getStrValue(FAST_DFS_TRACKERS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FastDfsClient.init(DFS_CONFIG);
    }
}

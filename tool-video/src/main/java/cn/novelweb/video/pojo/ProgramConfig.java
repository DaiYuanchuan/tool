package cn.novelweb.video.pojo;

import cn.novelweb.tool.upload.fastdfs.utils.Log;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>程序基础配置</p>
 * <p>2020-02-24 18:44</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProgramConfig {

    /**
     * 默认命令行执行根路径
     */
    private String path = org.bytedeco.javacpp.Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);

    /**
     * 任务池大小
     */
    private Integer size = 10;

    /**
     * 是否开启保活
     */
    private boolean keepalive;

    /**
     * 是否输出deBug日志
     */
    private boolean debugLog = Log.debugLog;

    /**
     * 设置日志
     * @param debugLog 是否输出日志信息
     */
    public void setDeBugLog(boolean debugLog) {
        Log.debugLog = debugLog;
    }
}

package cn.novelweb.tool.video.command.assemble;

import java.util.Map;

/**
 * <p>命令组装器接口</p>
 * <p>2020-02-24 22:32</p>
 *
 * @author Dai Yuanchuan
 **/
public interface CommandAssemble {

    /**
     * 将参数转为ffmpeg命令
     * @param paramMap map组装的ffmpeg命令参数
     * @return 返回一个完整的命令行,组装失败时返回NULL
     */
    String assemble(Map<String, String> paramMap);

}

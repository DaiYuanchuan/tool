package cn.novelweb.tool.video.command.assemble;

/**
 * <p>流式命令行构建器</p>
 * <p>2020-02-24 23:16</p>
 *
 * @author Dai Yuanchuan
 **/
public interface CommandBuilder {

    /**
     * 创建命令行
     *
     * @param root 命令行运行根目录或FFmpeg可执行文件安装目录
     * @return 命令行构建器CommandBuilder
     */
    CommandBuilder create(String root);

    /**
     * 创建默认根目录或默认FFmpeg可执行文件安装目录
     *
     * @return 命令行构建器CommandBuilder
     */
    CommandBuilder create();

    /**
     * 累加键-值命令
     *
     * @param key 键
     * @param val 命令
     * @return 命令行构建器CommandBuilder
     */
    CommandBuilder add(String key, String val);

    /**
     * 累加命令
     *
     * @param val 命令
     * @return 命令行构建器CommandBuilder
     */
    CommandBuilder add(String val);

    /**
     * 生成完整的命令行
     *
     * @return 命令行构建器CommandBuilder
     */
    CommandBuilder build();

    /**
     * 获取已经构建好的命令行
     *
     * @return 命令行构建器CommandBuilder
     */
    String get();

}

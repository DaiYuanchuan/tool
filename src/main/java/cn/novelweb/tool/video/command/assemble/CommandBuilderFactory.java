package cn.novelweb.tool.video.command.assemble;

/**
 * <p>默认流式命令构建器工厂类</p>
 * <p>2020-02-24 23:23</p>
 *
 * @author Dai Yuanchuan
 **/
public class CommandBuilderFactory {

    public static CommandBuilder create() {
        return new CommandBuilderImpl();
    }

    public static CommandBuilder create(String rootPath) {
        return new CommandBuilderImpl(rootPath);
    }

}

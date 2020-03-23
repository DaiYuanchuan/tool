package cn.novelweb.video.command.assemble;

import cn.novelweb.tool.upload.fastdfs.utils.Log;
import cn.novelweb.video.command.handler.CommandHandlerImpl;

/**
 * <p>默认流式命令行构建器</p>
 * <p>非线程安全实现</p>
 * <p>2020-02-24 23:34</p>
 *
 * @author Dai Yuanchuan
 **/
public class CommandBuilderImpl implements CommandBuilder {

    StringBuilder stringBuilder;
    String command;

    public CommandBuilderImpl() {
        create();
    }

    public CommandBuilderImpl(String rootPath) {
        create(rootPath);
    }

    @Override
    public CommandBuilder create(String root) {
        stringBuilder = new StringBuilder(root);
        return this;
    }

    @Override
    public CommandBuilder create() {
        if (CommandHandlerImpl.programConfig == null) {
            Log.debug("命令行创建失败,程序基础配置为null");
            return null;
        }
        return create(CommandHandlerImpl.programConfig.getPath());
    }

    @Override
    public CommandBuilder add(String key, String val) {
        return add(key).add(val);
    }

    @Override
    public CommandBuilder add(String val) {
        if (stringBuilder != null) {
            addBlank();
            stringBuilder.append(val);
        } else {
            Log.debug("累加命令失败,请先create()");
        }
        return this;
    }

    @Override
    public CommandBuilder build() {
        if (stringBuilder != null) {
            command = stringBuilder.toString();
        } else {
            Log.debug("累加命令失败,请先create()");
        }
        return this;
    }

    private void addBlank() {
        stringBuilder.append(" ");
    }

    @Override
    public String get() {
        if (command == null) {
            build();
        }
        return command;
    }
}

package cn.novelweb.video.command.handler;

import cn.novelweb.tool.upload.fastdfs.utils.Log;
import cn.novelweb.video.command.util.ExecUtil;
import cn.novelweb.video.pojo.CommandTask;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * <p>任务处理的实现</p>
 * <p>2020-02-24 21:54</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class TaskHandlerImpl implements TaskHandler {

    private OutHandlerMethod outHandlerMethod;

    public TaskHandlerImpl(OutHandlerMethod outHandlerMethod) {
        this.outHandlerMethod = outHandlerMethod;
    }

    public void setOutHandlerMethod(OutHandlerMethod outHandlerMethod) {
        this.outHandlerMethod = outHandlerMethod;
    }

    @Override
    public CommandTask process(String taskId, String command) {
        try {
            CommandTask commandTask = ExecUtil.createTask(taskId, command, outHandlerMethod);
            Log.debug("任务id:{},执行命令:{}", taskId, command);
            return commandTask;
        } catch (IOException e) {
            Log.debug("任务id:{},执行命令失败！进程和输出线程已停止", taskId);
            // 出现异常说明开启失败，返回null
            return null;
        }
    }

    @Override
    public boolean stop(Process process) {
        return ExecUtil.stop(process);
    }
}

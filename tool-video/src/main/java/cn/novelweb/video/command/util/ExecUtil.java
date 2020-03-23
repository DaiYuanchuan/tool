package cn.novelweb.video.command.util;

import cn.novelweb.video.command.handler.OutHandler;
import cn.novelweb.video.command.handler.OutHandlerMethod;
import cn.novelweb.video.pojo.CommandTask;

import java.io.IOException;

/**
 * <p>命令行操作工具类</p>
 * <p>2020-02-24 20:00</p>
 *
 * @author Dai Yuanchuan
 **/
public class ExecUtil {

    /**
     * 执行命令行并获取进程
     *
     * @param cmd 需要执行的命令行
     * @return 返回Process类
     * @throws IOException IO异常
     */
    public static Process exec(String cmd) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        // 执行命令获取主进程
        return runtime.exec(cmd);
    }

    /**
     * 销毁进程
     *
     * @param process Process类
     * @return 参数类为null时 返回false:销毁失败
     */
    public static boolean stop(Process process) {
        if (process != null) {
            process.destroy();
            return true;
        }
        return false;
    }

    /**
     * 销毁任务
     *
     * @param task 任务线程
     */
    public static void stop(CommandTask task) {
        if (task != null) {
            stop(task.getProcess());
        }
    }

    /**
     * 创建命令行数据
     *
     * @param taskId           任务id
     * @param command          命令
     * @param outHandlerMethod 任务消息输出接口
     * @return
     */
    public static CommandTask createTask(String taskId, String command,
                                         OutHandlerMethod outHandlerMethod) throws IOException {
        // 执行本地命令获取任务主进程
        Process process = exec(command);
        // 创建输出线程
        OutHandler outHandler = OutHandler.create(process.getErrorStream(), taskId, outHandlerMethod);
        return new CommandTask(taskId, command, process, outHandler);
    }

    /**
     * 中断故障缘故重启
     *
     * @param commandTask 任务实体
     */
    public static void restart(CommandTask commandTask) throws IOException {
        if (commandTask != null) {
            String taskId = commandTask.getTaskId(), command = commandTask.getCommand();
            OutHandlerMethod outHandlerMethod = null;
            if (commandTask.getThread() != null) {
                outHandlerMethod = commandTask.getThread().getOutHandlerMethod();
            }

            //安全销毁命令行进程和输出子线程
            stop(commandTask);
            // 执行本地命令获取任务主进程
            Process process = exec(command);
            commandTask.setProcess(process);
            // 创建输出线程
            OutHandler outHandler = OutHandler.create(process.getErrorStream(), taskId, outHandlerMethod);
            commandTask.setThread(outHandler);
        }
    }
}

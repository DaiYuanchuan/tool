package cn.novelweb.tool.video.command.handler;

import cn.novelweb.tool.video.pojo.CommandTask;

/**
 * <p>任务执行接口</p>
 * <p>2020-02-24 21:50</p>
 *
 * @author Dai Yuanchuan
 **/
public interface TaskHandler {

    /**
     * 按照命令执行主进程和输出线程
     *
     * @param taskId  任务id
     * @param command 命令
     * @return 返回 CommandTask 任务主进程
     */
    CommandTask process(String taskId, String command);

    /**
     * 停止主进程
     * 停止主进程需要保证输出线程已经关闭，否则输出线程会出错
     *
     * @param process 执行过程
     * @return 是否停止成功
     */
    boolean stop(Process process);
}

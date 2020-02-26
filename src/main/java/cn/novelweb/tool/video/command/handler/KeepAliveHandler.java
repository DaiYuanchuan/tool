package cn.novelweb.tool.video.command.handler;

import cn.novelweb.tool.upload.fastdfs.utils.Log;
import cn.novelweb.tool.video.command.task.TaskDao;
import cn.novelweb.tool.video.command.util.ExecUtil;
import cn.novelweb.tool.video.pojo.CommandTask;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>任务保活处理器</p>
 * <p>一个后台保活线程,用于处理异常中断的持久任务</p>
 * <p>2020-02-24 19:24</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class KeepAliveHandler extends Thread {

    /**
     * 待处理队列
     */
    private static Queue<String> queue;

    /**
     * 错误计数
     */
    public int errIndex = 0;

    /**
     * 安全停止线程标记
     */
    public volatile int stopIndex = 0;

    /**
     * 持久化处理器
     */
    private TaskDao taskDao;

    public KeepAliveHandler(TaskDao taskDao) {
        super();
        this.taskDao = taskDao;
        queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * 添加待处理任务
     *
     * @param taskId 任务id
     */
    public static void add(String taskId) {
        if (queue != null) {
            queue.offer(taskId);
        }
    }

    /**
     * 停止
     *
     * @param process 任务
     * @return 是否停止了
     */
    public boolean stop(Process process) {
        if (process != null) {
            process.destroy();
            return true;
        }
        return false;
    }

    @Override
    public void interrupt() {
        stopIndex = 1;
    }

    @Override
    public void run() {
        for (; stopIndex == 0; ) {
            if (queue == null) {
                continue;
            }
            String taskId;
            CommandTask task;
            try {
                while (queue.peek() != null) {
                    Log.debug("准备重启任务:{}", queue);
                    taskId = queue.poll();
                    if (this.isInterrupted()) {
                        Log.debug("任务id:{},线程已关闭", taskId);
                        return;
                    }
                    task = taskDao.get(taskId);
                    // 重启任务
                    ExecUtil.restart(task);
                }
            } catch (IOException e) {
                Log.debug("任务id:{},任务重启失败");
                //重启任务失败
                errIndex++;
            }
        }
    }
}

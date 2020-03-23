package cn.novelweb.video.command.handler;

import cn.novelweb.tool.upload.fastdfs.utils.Log;

/**
 * <p>任务消息输出处理</p>
 * <p>2020-02-24 23:52</p>
 *
 * @author Dai Yuanchuan
 **/
public class TaskMessageOutputProcessing implements OutHandlerMethod {

    /**
     * 任务是否异常中断
     */
    public boolean isBroken = false;

    @Override
    public void parsing(String taskId, String msg) {
        int fail = msg.indexOf("fail");
        if (fail != -1) {
            Log.debug("任务id:{},任务中断,任务可能发生故障:{}", taskId, msg);
            isBroken = true;
            return;
        }
        int miss = msg.indexOf("miss");
        if (miss != -1) {
            Log.debug("任务id:{},任务中断,任务可能发生丢包:{}", taskId, msg);
            isBroken = true;
            return;
        }
        int error = msg.indexOf("Error");
        if (error != -1) {
            Log.debug("任务id:{},任务中断,任务可能发生故障:{}", taskId, msg);
            isBroken = true;
            return;
        }
        Log.debug("任务id:{},{}", taskId, msg);
    }

    @Override
    public boolean isBroken() {
        return isBroken;
    }
}

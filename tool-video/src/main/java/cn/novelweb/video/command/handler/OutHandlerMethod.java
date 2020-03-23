package cn.novelweb.video.command.handler;

/**
 * <p>输出消息处理接口</p>
 * <p>2020-02-24 18:08</p>
 *
 * @author Dai Yuanchuan
 **/
public interface OutHandlerMethod {

    /**
     * 解析消息
     *
     * @param taskId 消息的任务id
     * @param msg    消息
     */
    void parsing(String taskId, String msg);

    /**
     * 任务是否异常中断
     *
     * @return true 任务中断
     */
    boolean isBroken();

}

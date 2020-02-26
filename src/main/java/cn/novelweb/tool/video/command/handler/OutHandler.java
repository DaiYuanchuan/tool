package cn.novelweb.tool.video.command.handler;

import cn.novelweb.tool.upload.fastdfs.utils.Log;
import lombok.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * <p>任务消息输出处理器</p>
 * <p>2020-02-24 17:58</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@ToString(callSuper = true)
public class OutHandler extends Thread {

    /**
     * 控制器状态
     */
    private volatile boolean desStatus = true;

    /**
     * 读取任务输出流
     */
    private BufferedReader bufferedReader;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 消息处理方法
     */
    private OutHandlerMethod outHandlerMethod;

    /**
     * 创建输出线程(默认立即开启线程)
     *
     * @param inputStream      input流
     * @param taskId           任务id
     * @param outHandlerMethod 输出消息处理接口
     * @return 任务消息输出处理器
     */
    public static OutHandler create(InputStream inputStream, String taskId, OutHandlerMethod outHandlerMethod) {
        return create(inputStream, taskId, outHandlerMethod, true);
    }

    /**
     * 创建输出线程
     * 指定启动线程开启
     *
     * @param inputStream      input流
     * @param taskId           任务id
     * @param outHandlerMethod 输出消息处理接口
     * @param start            是否需要启动任务线程
     * @return 任务消息输出处理器
     */
    public static OutHandler create(InputStream inputStream, String taskId,
                                    OutHandlerMethod outHandlerMethod, boolean start) {
        OutHandler out = new OutHandler(inputStream, taskId, outHandlerMethod);
        if (start) {
            out.start();
        }
        return out;
    }

    public OutHandler(InputStream inputStream, String taskId, OutHandlerMethod outHandlerMethod) {
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        this.taskId = taskId;
        this.outHandlerMethod = outHandlerMethod;
    }

    /**
     * 执行线程
     */
    @Override
    public void run() {
        if (this.isInterrupted()) {
            Log.debug("任务id:{},线程已关闭", taskId);
            return;
        }
        String message;
        try {
            Log.debug("任务id:{},开始执行任务.", taskId);
            while (desStatus && (message = bufferedReader.readLine()) != null) {
                outHandlerMethod.parsing(taskId, message);
                if (outHandlerMethod.isBroken()) {
                    Log.debug("任务id:{},检测到中断,提交重启任务给保活处理器.", taskId);
                    //如果发生异常中断，立即进行保活
                    //把中断的任务交给保活处理器进行进一步处理
                    KeepAliveHandler.add(taskId);
                }
            }
        } catch (IOException e) {
            Log.debug("任务id:{},发生内部异常错误,线程标记停止状态", taskId);
            this.interrupt();
        }
    }
}

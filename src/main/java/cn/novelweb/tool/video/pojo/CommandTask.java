package cn.novelweb.tool.video.pojo;

import cn.novelweb.tool.video.command.handler.OutHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>用于存放任务id,任务主进程,任务输出线程</p>
 * <p>2020-02-24 17:43</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CommandTask {

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 具体命令行
     */
    private String command;

    /**
     * 命令行运行主进程
     */
    private Process process;

    /**
     * 命令行消息输出子线程
     */
    private OutHandler thread;

}

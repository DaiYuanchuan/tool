package cn.novelweb.video.command.handler;

import cn.novelweb.video.command.assemble.CommandAssemble;
import cn.novelweb.video.command.assemble.CommandBuilder;
import cn.novelweb.video.command.task.TaskDao;
import cn.novelweb.video.pojo.CommandTask;

import java.util.Collection;
import java.util.Map;

/**
 * <p>命令行管理器接口</p>
 * <p>Fast Forward Moving Picture Experts Group命令行操作管理器</p>
 * <p>可执行Fast Forward Moving Picture Experts Group命令/停止/查询任务信息</p>
 * <p>该包需要配合对应系统版本的Fast Forward Moving Picture Experts Group</p>
 * <p>下载编译好的对应的系统版本:https://ffmpeg.zeranoe.com/builds/</p>
 * <p>2020-02-24 17:11</p>
 *
 * @author Dai Yuanchuan
 **/
public interface CommandHandler {

    /**
     * 注入自己实现的任务信息持久层接口
     *
     * @param taskDao 任务信息持久层接口
     */
    void setTaskDao(TaskDao taskDao);

    /**
     * 注入ffmpeg命令处理器
     *
     * @param taskHandler 任务执行接口
     */
    void setTaskHandler(TaskHandler taskHandler);

    /**
     * 注入ffmpeg命令组装器
     *
     * @param commandAssemble 命令组装器接口
     */
    void setCommandAssemble(CommandAssemble commandAssemble);

    /**
     * 通过命令发布任务（默认命令前不加FFmpeg路径）
     *
     * @param taskId  任务标识
     * @param command FFmpeg命令
     * @return 命令字符串
     */
    String start(String taskId, String command);

    /**
     * 通过命令发布任务
     *
     * @param taskId  任务标识
     * @param command FFmpeg命令
     * @param hasPath 命令中是否包含FFmpeg执行文件的绝对路径
     * @return 命令字符串
     */
    String start(String taskId, String command, boolean hasPath);

    /**
     * 通过流式命令构建器发布任务
     *
     * @param taskId         任务标识
     * @param commandBuilder 流式命令行构建器
     * @return 命令字符串
     */
    String start(String taskId, CommandBuilder commandBuilder);

    /**
     * 通过组装命令发布任务
     *
     * @param map 组装命令
     * @return 命令字符串
     */
    String start(Map<String, String> map);

    /**
     * 停止任务
     *
     * @param taskId 任务id
     * @return 停止是否成功
     */
    boolean stop(String taskId);

    /**
     * 停止全部任务
     *
     * @return 停止的数量
     */
    int stopAll();

    /**
     * 通过id查询任务信息
     *
     * @param taskId 任务id
     * @return 任务实体CommandTask信息
     */
    CommandTask get(String taskId);

    /**
     * 获取全部任务信息
     *
     * @return 全部任务信息集合
     */
    Collection<CommandTask> getAll();

    /**
     * 销毁一些后台资源和保活线程
     */
    void destroy();

}

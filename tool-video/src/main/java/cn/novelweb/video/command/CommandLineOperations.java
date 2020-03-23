package cn.novelweb.video.command;

import cn.hutool.core.map.MapUtil;
import cn.novelweb.video.command.assemble.CommandBuilder;
import cn.novelweb.video.command.handler.CommandHandler;
import cn.novelweb.video.command.handler.CommandHandlerImpl;
import cn.novelweb.video.pojo.CommandTask;
import cn.novelweb.video.pojo.ProgramConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * <p>Fast Forward Moving Picture Experts Group命令行操作入口类</p>
 * <p>可执行Fast Forward Moving Picture Experts Group命令/停止/查询任务信息</p>
 * <p>该类需要配合对应系统版本的Fast Forward Moving Picture Experts Group</p>
 * <p>下载编译好的对应的系统版本:https://ffmpeg.zeranoe.com/builds/</p>
 * <p>2020-02-25 18:40</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class CommandLineOperations {

    /**
     * 命令行管理器的接口
     */
    private static CommandHandler commandHandler;

    /**
     * 是否成功初始化
     */
    private static boolean isSuccessInit = false;

    /**
     * 初始化配置类信息
     *
     * @param programConfig 初始化配置
     */
    public static void init(ProgramConfig programConfig) {
        if (programConfig == null) {
            log.error("入口参数:programConfig为null");
            return;
        }
        CommandHandlerImpl.programConfig = programConfig;
        commandHandler = new CommandHandlerImpl();
        isSuccessInit = true;
    }

    /**
     * 发布命令行任务
     *
     * @param taskId  任务id、任务标识、任务名称
     * @param command 需要执行的命令
     * @return 命令字符串
     */
    public static String start(String taskId, String command) {
        if (!isSuccessInit) {
            log.error("请先调用.init()方法初始化配置");
            return null;
        }
        if (StringUtils.isBlank(taskId) && StringUtils.isNotBlank(command)) {
            log.error("参数值为空");
            return null;
        }
        return commandHandler.start(taskId, command);
    }

    /**
     * 发布命令行任务
     *
     * @param taskId  任务id、任务标识、任务名称
     * @param command 需要执行的命令
     * @param hasPath 命令中是否包含FFmpeg执行文件的绝对路径
     *                如true:/usr/local/ff/ffmpeg
     * @return 命令字符串
     */
    public static String start(String taskId, String command, boolean hasPath) {
        if (!isSuccessInit) {
            log.error("请先调用.init()方法初始化配置");
            return null;
        }
        if (StringUtils.isBlank(taskId) && StringUtils.isNotBlank(command)) {
            log.error("参数值为空");
            return null;
        }
        return commandHandler.start(taskId, command, hasPath);
    }

    /**
     * 发布命令行任务
     *
     * @param taskId         任务id、任务标识、任务名称
     * @param commandBuilder 流式命令行构建器
     * @return 命令字符串
     */
    public static String start(String taskId, CommandBuilder commandBuilder) {
        if (!isSuccessInit) {
            log.error("请先调用.init()方法初始化配置");
            return null;
        }
        if (StringUtils.isBlank(taskId) && commandBuilder == null) {
            log.error("参数值为空");
            return null;
        }
        return commandHandler.start(taskId, commandBuilder);
    }

    /**
     * 发布命令行任务
     *
     * @param map 组装map命令集合
     * @return 命令字符串
     */
    public static String start(Map<String, String> map) {
        if (!isSuccessInit) {
            log.error("请先调用.init()方法初始化配置");
            return null;
        }
        if (MapUtil.isEmpty(map)) {
            log.error("参数map值为空");
            return null;
        }
        return commandHandler.start(map);
    }

    public static boolean stop(String taskId) {
        if (!isSuccessInit) {
            log.error("请先调用.init()方法初始化配置");
            return false;
        }
        if (StringUtils.isBlank(taskId)) {
            log.error("参数taskId值为空");
            return false;
        }
        return commandHandler.stop(taskId);
    }

    /**
     * 停止全部任务
     *
     * @return 停止的数量
     */
    public static int stopAll() {
        if (!isSuccessInit) {
            log.error("请先调用.init()方法初始化配置");
            return -1;
        }
        return commandHandler.stopAll();
    }

    /**
     * 通过id查询任务信息
     *
     * @param taskId 任务id
     * @return 任务实体CommandTask信息
     */
    public static CommandTask get(String taskId) {
        if (!isSuccessInit) {
            log.error("请先调用.init()方法初始化配置");
            return null;
        }
        if (StringUtils.isBlank(taskId)) {
            log.error("参数taskId值为空");
            return null;
        }
        return commandHandler.get(taskId);
    }

    /**
     * 获取全部任务信息
     *
     * @return 全部任务信息集合
     */
    public static Collection<CommandTask> getAll() {
        if (!isSuccessInit) {
            log.error("请先调用.init()方法初始化配置");
            return null;
        }
        return commandHandler.getAll();
    }

    /**
     * 销毁一些后台资源和保活线程
     */
    public static void destroy() {
        if (!isSuccessInit) {
            log.error("请先调用.init()方法初始化配置");
            return;
        }
        commandHandler.destroy();
    }
}

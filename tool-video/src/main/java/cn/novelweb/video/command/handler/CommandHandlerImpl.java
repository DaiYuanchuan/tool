package cn.novelweb.video.command.handler;

import cn.hutool.core.map.MapUtil;
import cn.novelweb.tool.upload.fastdfs.utils.Log;
import cn.novelweb.video.command.assemble.CommandAssemble;
import cn.novelweb.video.command.assemble.CommandAssembleImpl;
import cn.novelweb.video.command.assemble.CommandBuilder;
import cn.novelweb.video.command.task.TaskDao;
import cn.novelweb.video.command.task.TaskDaoImpl;
import cn.novelweb.video.pojo.CommandTask;
import cn.novelweb.video.pojo.ProgramConfig;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>命令行管理器接口的实现</p>
 * <p>2020-02-25 08:48</p>
 *
 * @author Dai Yuanchuan
 **/
public class CommandHandlerImpl implements CommandHandler {

    /**
     * 任务持久化器
     */
    private TaskDao taskDao;

    /**
     * 任务执行处理器
     */
    private TaskHandler taskHandler;

    /**
     * 命令组装器
     */
    private CommandAssemble commandAssemble;

    /**
     * 任务消息处理器
     */
    private OutHandlerMethod outHandlerMethod;

    /**
     * 保活处理器
     */
    private KeepAliveHandler keepAliveHandler;

    /**
     * 程序基础配置
     */
    public static ProgramConfig programConfig = new ProgramConfig();

    public CommandHandlerImpl() {
        this(null);
    }

    /**
     * 指定任务池大小的初始化，其他使用默认
     *
     * @param size 任务池大小
     */
    public CommandHandlerImpl(Integer size) {
        init(size);
    }

    public CommandHandlerImpl(TaskDao taskDao, TaskHandler taskHandler, CommandAssemble commandAssemble,
                              OutHandlerMethod outHandlerMethod, Integer size) {
        super();
        this.taskDao = taskDao;
        this.taskHandler = taskHandler;
        this.commandAssemble = commandAssemble;
        this.outHandlerMethod = outHandlerMethod;
        init(size);
    }

    /**
     * 初始化，如果几个处理器未注入，则使用默认处理器
     *
     * @param size
     */
    public void init(Integer size) {
        boolean isKeepalive = false;
        if (size == null) {
            size = programConfig.getSize() == null ? 10 : programConfig.getSize();
            isKeepalive = programConfig.isKeepalive();
        }

        if (this.outHandlerMethod == null) {
            this.outHandlerMethod = new TaskMessageOutputProcessing();
        }

        if (this.taskDao == null) {
            this.taskDao = new TaskDaoImpl(size);
            //初始化保活线程
            if (isKeepalive) {
                keepAliveHandler = new KeepAliveHandler(taskDao);
                keepAliveHandler.start();
            }
        }

        if (this.taskHandler == null) {
            this.taskHandler = new TaskHandlerImpl(this.outHandlerMethod);
        }

        if (this.commandAssemble == null) {
            this.commandAssemble = new CommandAssembleImpl();
        }
    }

    @Override
    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    @Override
    public void setTaskHandler(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    @Override
    public void setCommandAssemble(CommandAssemble commandAssemble) {
        this.commandAssemble = commandAssemble;
    }

    /**
     * 是否已经初始化
     *
     * @param b 如果未初始化时是否初始化
     * @return 是否成功完成初始化操作
     */
    public boolean isInit(boolean b) {
        boolean ret = this.outHandlerMethod == null || this.taskDao == null ||
                this.taskHandler == null || this.commandAssemble == null;
        if (ret && b) {
            init(null);
        }
        return ret;
    }

    @Override
    public String start(String taskId, String command) {
        return start(taskId, command, false);
    }

    @Override
    public String start(String taskId, String command, boolean hasPath) {
        if (isInit(true)) {
            Log.debug("执行失败,未进行初始化或初始化失败!");
            return null;
        }
        if (taskId != null && command != null) {
            CommandTask commandTask = taskHandler.process(taskId, hasPath ? command : programConfig.getPath() + command);
            if (commandTask != null && !commandTask.getThread().isInterrupted()) {
                int ret = taskDao.add(commandTask);
                if (ret > 0) {
                    return commandTask.getTaskId();
                } else {
                    // 持久化信息失败，停止处理
                    taskHandler.stop(commandTask.getProcess());
                    commandTask.getThread().interrupt();
                    Log.debug("持久化失败,停止任务!");
                }
            }
        }
        return null;
    }

    @Override
    public String start(String taskId, CommandBuilder commandBuilder) {
        String command = commandBuilder.get();
        if (command != null) {
            return start(taskId, command, true);
        }
        return null;
    }

    @Override
    public String start(Map<String, String> map) {
        if (MapUtil.isEmpty(map)) {
            Log.debug("参数不正确,无法执行");
            return null;
        }
        String appName = map.get("appName");
        if (StringUtils.isBlank(appName)) {
            Log.debug("appName不能为空");
            return null;
        }

        map.put("FFMPEGPath", programConfig.getPath());
        String command = commandAssemble.assemble(map);
        if (command != null) {
            return start(appName, command, true);
        }
        return null;
    }

    @Override
    public boolean stop(String taskId) {
        if (taskId != null && taskDao.isHave(taskId)) {
            Log.debug("任务id:{},正在尝试停止任务", taskId);
            CommandTask commandTask = taskDao.get(taskId);
            commandTask.getThread().interrupt();
            if (taskHandler.stop(commandTask.getProcess())) {
                taskDao.remove(taskId);
                return true;
            }
        }
        Log.debug("任务id:{},停止任务失败", taskId);
        return false;
    }

    @Override
    public int stopAll() {
        Collection<CommandTask> list = taskDao.getAll();
        Iterator<CommandTask> iter = list.iterator();
        CommandTask commandTask;
        int index = 0;
        while (iter.hasNext()) {
            commandTask = iter.next();
            commandTask.getThread().interrupt();
            if (taskHandler.stop(commandTask.getProcess())) {
                taskDao.remove(commandTask.getTaskId());
                index++;
            }
        }
        Log.debug("尝试停止了{}个任务", index);
        return index;
    }

    @Override
    public CommandTask get(String taskId) {
        return taskDao.get(taskId);
    }

    @Override
    public Collection<CommandTask> getAll() {
        return taskDao.getAll();
    }

    @Override
    public void destroy() {
        if (keepAliveHandler != null) {
            // 安全停止保活线程
            keepAliveHandler.interrupt();
        }
    }
}

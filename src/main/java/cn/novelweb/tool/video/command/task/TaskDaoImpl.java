package cn.novelweb.tool.video.command.task;

import cn.novelweb.tool.video.pojo.CommandTask;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>任务信息持久层实现</p>
 * <p>2020-02-24 23:44</p>
 *
 * @author Dai Yuanchuan
 **/
public class TaskDaoImpl implements TaskDao {

    /**
     * 存放任务信息
     */
    private ConcurrentMap<String, CommandTask> commandTaskConcurrentMap;

    public TaskDaoImpl(int size) {
        commandTaskConcurrentMap = new ConcurrentHashMap<>(size);
    }

    @Override
    public CommandTask get(String taskId) {
        return commandTaskConcurrentMap.get(taskId);
    }

    @Override
    public Collection<CommandTask> getAll() {
        return commandTaskConcurrentMap.values();
    }

    @Override
    public int add(CommandTask commandTask) {
        String id = commandTask.getTaskId();
        if (id != null && !commandTaskConcurrentMap.containsKey(id)) {
            commandTaskConcurrentMap.put(commandTask.getTaskId(), commandTask);
            if (commandTaskConcurrentMap.get(id) != null) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public int remove(String taskId) {
        if (commandTaskConcurrentMap.remove(taskId) != null) {
            return 1;
        }
        return 0;
    }

    @Override
    public int removeAll() {
        int size = commandTaskConcurrentMap.size();
        try {
            commandTaskConcurrentMap.clear();
        } catch (Exception e) {
            return 0;
        }
        return size;
    }

    @Override
    public boolean isHave(String taskId) {
        return commandTaskConcurrentMap.containsKey(taskId);
    }
}

package cn.novelweb.video.command.task;

import cn.novelweb.video.pojo.CommandTask;

import java.util.Collection;

/**
 * <p>任务信息持久层接口</p>
 * <p>2020-02-24 17:42</p>
 *
 * @author Dai Yuanchuan
 **/
public interface TaskDao {

    /**
     * 通过id查询任务信息
     *
     * @param taskId 任务id
     * @return CommandTask任务实体
     */
    CommandTask get(String taskId);

    /**
     * 查询全部任务信息
     *
     * @return CommandTask任务实体集合
     */
    Collection<CommandTask> getAll();

    /**
     * 增加任务信息
     *
     * @param commandTask 任务信息实体
     * @return 增加数量：<1-增加失败，>=1-增加成功
     */
    int add(CommandTask commandTask);

    /**
     * 删除 任务id 对应的任务信息
     *
     * @param taskId 任务id
     * @return 数量：<1-操作失败，>=1-操作成功
     */
    int remove(String taskId);

    /**
     * 删除全部任务信息
     *
     * @return 数量：<1-操作失败，>=1-操作成功
     */
    int removeAll();

    /**
     * 判断是否存在某个ID
     *
     * @param taskId 任务id
     * @return true:存在  false:不存在
     */
    boolean isHave(String taskId);

}

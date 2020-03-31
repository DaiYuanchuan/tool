package cn.novelweb.tool.cron;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.io.resource.NoResourceException;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.cron.Scheduler;
import cn.hutool.cron.pattern.CronPattern;
import cn.hutool.cron.task.Task;
import cn.hutool.setting.Setting;
import cn.hutool.setting.SettingRuntimeException;
import cn.novelweb.tool.cron.pojo.TaskParam;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>定时任务工具包</p>
 * <p>2020-03-27 10:37</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class TimingTask {

    /**
     * 一个全局的任务调度器，所有定时任务在同一个调度器中执行
     */
    private static final Scheduler SCHEDULER = new Scheduler();

    /**
     * 锁
     */
    private static final Lock LOCK = new ReentrantLock();

    /**
     * CronTab配置文件
     */
    public static final String CRON_TAB_CONFIG_PATH = "config/cron.setting";
    public static final String CRON_TAB_CONFIG_PATH2 = "cron.setting";
    private static Setting cronTabSetting;

    private TimingTask() {
    }

    /**
     * 自定义定时任务配置文件
     *
     * @param cronSetting 定时任务配置文件
     */
    public static void setCronSetting(Setting cronSetting) {
        cronTabSetting = cronSetting;
    }

    /**
     * 设置是否支持秒匹配<br>
     * 此方法用于定义是否使用秒匹配模式，如果为true，则定时任务表达式中的第一位为秒，否则为分，默认是分<br>
     *
     * @param isMatchSecond <code>true</code>支持，<code>false</code>不支持
     */
    public static void setMatchSecond(boolean isMatchSecond) {
        SCHEDULER.setMatchSecond(isMatchSecond);
    }

    /**
     * 添加定时任务数据、加入定时任务
     *
     * @param id        定时任务ID
     * @param taskParam 需要启动的任务
     * @return 返回任务id
     */
    public static String schedule(String id, TaskParam taskParam) {
        SCHEDULER.schedule(id, taskParam.getCron(), (Task) () -> performTasks(taskParam));
        return id;
    }

    /**
     * 添加定时任务数据、加入定时任务
     *
     * @param taskParam 需要启动的任务
     * @return 返回任务id
     */
    public static String schedule(TaskParam taskParam) {
        return SCHEDULER.schedule(taskParam.getCron(), (Task) () -> performTasks(taskParam));
    }

    /**
     * 私有方法 执行任务
     *
     * @param taskParam 需要启动的任务
     */
    private static void performTasks(TaskParam taskParam) {
        // 获取需要执行任务的类
        final Class<?> clazz = ClassLoaderUtil.loadClass(taskParam.getClassPath());
        // 尝试遍历 并 调用该类的所有 构造方法
        Object object = ReflectUtil.newInstanceIfPossible(clazz);
        if (object == null) {
            log.error("获取到的类为NULL");
            return;
        }
        // 获取到指定的方法
        if (taskParam.getParam() == null) {
            ReflectUtil.invoke(object, taskParam.getMethodName());
            return;
        }
        ReflectUtil.invoke(object, taskParam.getMethodName(), taskParam.getParam());
    }

    /**
     * 加入定时任务
     *
     * @param schedulingPattern 定时任务执行时间的cronTab表达式
     * @param task              任务
     * @return 定时任务ID
     */
    public static String schedule(String schedulingPattern, Runnable task) {
        return SCHEDULER.schedule(schedulingPattern, task);
    }

    /**
     * 批量加入配置文件中的定时任务
     *
     * @param cronSetting 定时任务设置文件
     */
    public static void schedule(Setting cronSetting) {
        SCHEDULER.schedule(cronSetting);
    }

    /**
     * 移除任务
     *
     * @param schedulerId 任务ID
     */
    public static void remove(String schedulerId) {
        SCHEDULER.deschedule(schedulerId);
    }

    /**
     * 更新Task的执行时间规则
     *
     * @param id             Task的ID
     * @param cronExpression Cron 表达式
     * @since 4.0.10
     */
    public static void updatePattern(String id, String cronExpression) {
        SCHEDULER.updatePattern(id, new CronPattern(cronExpression));
    }

    /**
     * 获取计划程序
     *
     * @return 获得Scheduler对象
     */
    public static Scheduler getScheduler() {
        return SCHEDULER;
    }

    /**
     * 启动定时器、开始，非守护线程模式
     *
     * @see #start(boolean)
     */
    public static void start() {
        start(false);
    }

    /**
     * 开始
     *
     * @param isDaemon 是否以守护线程方式启动，
     *                 如果为true，则在调用{@link #stop()}方法后执行的定时任务立即结束，否则等待执行完毕才结束。
     */
    synchronized public static void start(boolean isDaemon) {
        if (SCHEDULER.isStarted()) {
            throw new UtilException("计划程序已启动，请先停止它!");
        }

        LOCK.lock();
        try {
            if (null == cronTabSetting) {
                // 尝试查找config/cron.setting
                setCronSetting(CRON_TAB_CONFIG_PATH);
            }
            // 尝试查找cron.setting
            if (null == cronTabSetting) {
                setCronSetting(CRON_TAB_CONFIG_PATH2);
            }
        } finally {
            LOCK.unlock();
        }
        schedule(cronTabSetting);
        SCHEDULER.start(isDaemon);
    }

    /**
     * 自定义定时任务配置文件路径
     *
     * @param cronSettingPath 定时任务配置文件路径（相对绝对都可）
     */
    public static void setCronSetting(String cronSettingPath) {
        try {
            cronTabSetting = new Setting(cronSettingPath, Setting.DEFAULT_CHARSET, false);
        } catch (SettingRuntimeException | NoResourceException e) {
            // 忽略设置文件分析错误和无配置错误
        }
    }

    /**
     * 重新启动定时任务<br>
     * 此方法会清除动态加载的任务，重新启动后，守护线程与否与之前保持一致
     */
    public static void restart() {
        LOCK.lock();
        try {
            if (null != cronTabSetting) {
                //重新读取配置文件
                cronTabSetting.load();
            }
            if (SCHEDULER.isStarted()) {
                //关闭并清除已有任务
                SCHEDULER.stop(true);
            }
        } finally {
            LOCK.unlock();
        }

        //重新加载任务
        schedule(cronTabSetting);
        //重新启动
        SCHEDULER.start();
    }

    /**
     * 停止所有的定时任务
     */
    public static void stop() {
        SCHEDULER.stop();
    }
}

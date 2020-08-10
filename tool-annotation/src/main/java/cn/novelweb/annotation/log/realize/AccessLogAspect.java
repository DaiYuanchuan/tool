package cn.novelweb.annotation.log.realize;

import cn.hutool.core.thread.ThreadUtil;
import cn.novelweb.annotation.TaskCallback;
import cn.novelweb.annotation.log.AccessLog;
import cn.novelweb.annotation.log.callback.AccessLogCompletionHandler;
import cn.novelweb.annotation.log.pojo.AccessLogInfo;
import cn.novelweb.annotation.log.util.Annotation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * <p>系统访问日志的注解实现类</p>
 * <p>2019-12-03 21:09</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
@Aspect
@Component
public class AccessLogAspect {

    /**
     * 配置织入点
     */
    @Pointcut("@annotation(cn.novelweb.annotation.log.AccessLog)")
    public void accessLog() {
    }

    /**
     * 前置通知 用于拦截操作
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "accessLog()")
    public void doBefore(JoinPoint joinPoint) {
        try {
            handleLog(joinPoint, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常信息
     */
    @AfterThrowing(value = "accessLog()", throwing = "e")
    public void doAfter(JoinPoint joinPoint, Exception e) {
        try {
            handleLog(joinPoint, e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleLog(final JoinPoint joinPoint, final Exception e) {
        // 获得注解信息
        AccessLog accessLog = Annotation.getAnnotation(joinPoint, AccessLog.class);
        if (accessLog == null) {
            log.info("Failed to get the annotation information correctly...");
            return;
        }
        // 初始化日志信息
        AccessLogInfo accessLogInfo = Annotation.initInfo(accessLog.title(),
                accessLog.isGetIp(), e);

        // 异步执行任务回调
        ThreadUtil.execAsync(() -> TaskCallback
                .callback(AccessLogCompletionHandler.class, accessLogInfo));
    }
}

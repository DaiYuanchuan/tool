package cn.novelweb.annotation.log.realize;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.novelweb.annotation.TaskCallback;
import cn.novelweb.annotation.log.OpLog;
import cn.novelweb.annotation.log.callback.OpLogCompletionHandler;
import cn.novelweb.annotation.log.pojo.OpLogInfo;
import cn.novelweb.annotation.log.util.Annotation;
import com.alibaba.fastjson.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * <p>操作日志的注解实现类</p>
 * <p>2019-12-05 20:30</p>
 *
 * @author Dai Yuanchuan
 **/
@Aspect
@Component
public class OpLogAspect {

    /**
     * 配置织入点
     */
    @Pointcut("@annotation(cn.novelweb.annotation.log.OpLog)")
    public void opLog() {
    }

    /**
     * 前置通知 用于拦截操作
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "opLog()")
    public void doBefore(JoinPoint joinPoint) {
        handleLog(joinPoint, null);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常信息
     */
    @AfterThrowing(value = "opLog()", throwing = "e")
    public void doAfter(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e);
    }

    private void handleLog(final JoinPoint joinPoint, final Exception e) {
        // 获得注解信息
        OpLog opLog = Annotation.getAnnotation(joinPoint, OpLog.class);
        if (opLog == null) {
            return;
        }

        // 初始化日志信息
        OpLogInfo opLogInfo = Convert.convert(OpLogInfo.class, Annotation.initInfo(opLog.title(),
                opLog.isGetIp(), e));
        // 设置业务类型、类名、方法名等
        opLogInfo.setBusinessType(opLog.businessType());
        opLogInfo.setClassName(joinPoint.getTarget().getClass().getName());
        opLogInfo.setMethodName(joinPoint.getSignature().getName());

        // 是否需要保存URL的请求参数
        if (opLog.isSaveRequestData()) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                opLogInfo.setParameter(JSONObject.toJSONString(
                        requestAttributes.getRequest().getParameterMap()));
            } else {
                opLogInfo.setParameter("无法获取request信息");
            }
        }
        // 异步执行任务回调
        ThreadUtil.execAsync(() -> TaskCallback
                .callback(OpLogCompletionHandler.class, opLogInfo));
    }
}

package cn.novelweb.annotation.log.realize;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.novelweb.annotation.TaskCallback;
import cn.novelweb.annotation.log.OpLog;
import cn.novelweb.annotation.log.callback.OpLogCompletionHandler;
import cn.novelweb.annotation.log.pojo.OpLogInfo;
import cn.novelweb.annotation.log.util.Annotation;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>操作日志的注解实现类</p>
 * <p>2019-12-05 20:30</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
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
    @AfterThrowing(value = "opLog()", throwing = "e")
    public void doAfter(JoinPoint joinPoint, Exception e) {
        try {
            handleLog(joinPoint, e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleLog(final JoinPoint joinPoint, final Exception e) {
        // 获得注解信息
        OpLog opLog = Annotation.getAnnotation(joinPoint, OpLog.class);
        if (opLog == null) {
            log.info("Failed to get the annotation information correctly...");
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
                // 如果获取到的请求参数为空
                if (MapUtil.isEmpty(requestAttributes.getRequest().getParameterMap())) {
                    Object[] args = joinPoint.getArgs();
                    // 判断是否为空
                    if (ArrayUtil.isEmpty(args)) {
                        opLogInfo.setParameter("");
                    } else {
                        List<Object> objectList = Arrays.asList(args);
                        // 尝试获取过滤后body中的参数
                        opLogInfo.setParameter(JSON.toJSONString(objectList.stream()
                                .filter(arg -> (!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)))
                                .collect(Collectors.toList())));
                    }
                } else {
                    opLogInfo.setParameter(JSONObject.toJSONString(requestAttributes.getRequest().getParameterMap()));
                }
            } else {
                opLogInfo.setParameter("无法获取request信息");
            }
        }
        // 异步执行任务回调
        ThreadUtil.execAsync(() -> TaskCallback
                .callback(OpLogCompletionHandler.class, opLogInfo));
    }
}

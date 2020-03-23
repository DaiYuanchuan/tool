package cn.novelweb.annotation.log.util;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.novelweb.annotation.log.pojo.AccessLogInfo;
import cn.novelweb.ip.IpUtils;
import cn.novelweb.ip.Region;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.Future;

/**
 * <p>封装一些关于日志注解需要用到的公共方法</p>
 * <p>2019-12-05 22:03</p>
 *
 * @author Dai Yuanchuan
 **/
public class Annotation {

    /**
     * 判断是否存在注解 存在就获取注解信息
     *
     * @param joinPoint       切点信息
     * @param annotationClass 需要获取的注解类
     * @param <A>             注解信息
     * @return 如果存在则返回注解信息，否则返回NULL
     */
    public static <A extends java.lang.annotation.Annotation> A getAnnotation
    (JoinPoint joinPoint, Class<A> annotationClass) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(annotationClass);
        }
        return null;
    }

    /**
     * 初始化日志信息
     *
     * @param title   模块名称
     * @param isGetIp 是否需要获取用户IP的实际地理位置
     * @param e       异常信息
     * @return 返回带有默认数据的日志信息
     */
    public static AccessLogInfo initInfo(final String title, final boolean isGetIp, final Exception e) {
        // 获取Request信息
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();

        // 初始化日志信息
        AccessLogInfo accessLogInfo = AccessLogInfo.builder()
                .ip("127.0.0.1")
                .requestUri("")
                .location("0-0-内网IP 内网IP")
                .build();

        String userAgent = "无法获取User-Agent信息";

        if (requestAttributes != null) {
            userAgent = requestAttributes.getRequest().getHeader("User-Agent");
            accessLogInfo.setIp(ServletUtil.getClientIP(requestAttributes.getRequest()));
            accessLogInfo.setRequestUri(requestAttributes.getRequest().getRequestURI());
        }

        Future<Region> future = null;
        if (isGetIp) {
            // 异步获取IP实际地理位置信息
            future = ThreadUtil.execAsync(() -> IpUtils.getIpLocationByBtree(accessLogInfo.getIp()));
        }

        // 获取/赋值 浏览器、os系统等信息
        UserAgent ua = UserAgentUtil.parse(userAgent);
        accessLogInfo.setBrowser(ua.getBrowser().toString());
        accessLogInfo.setBrowserVersion(ua.getVersion());
        accessLogInfo.setBrowserEngine(ua.getEngine().toString());
        accessLogInfo.setBrowserEngineVersion(ua.getEngineVersion());
        accessLogInfo.setIsMobile(ua.isMobile());
        accessLogInfo.setOs(ua.getOs().toString());
        accessLogInfo.setPlatform(ua.getPlatform().getName());
        accessLogInfo.setSpider(SpiderUtils.parseSpiderType(userAgent));

        // 访问的模块、状态、时间等
        accessLogInfo.setTitle(title);
        accessLogInfo.setStatus(e != null ? 1 : 0);
        accessLogInfo.setCreateTime(new Date());

        // 访问出现的异常信息
        accessLogInfo.setErrorCause(e != null ? e.getCause().toString() : "");
        accessLogInfo.setErrorMsg(e != null ? e.getCause().getMessage() : "");

        if (future != null) {
            try {
                Region region = future.get();
                accessLogInfo.setLocation(region.getCountry() + "-"
                        + region.getProvince() + "-" + region.getCity() + " "
                        + region.getIsp());
            } catch (Exception e1) {
                accessLogInfo.setErrorCause(e1.getCause().toString());
                accessLogInfo.setErrorMsg(e1.getCause().getMessage());
                accessLogInfo.setStatus(1);
            }
        }
        return accessLogInfo;
    }
}

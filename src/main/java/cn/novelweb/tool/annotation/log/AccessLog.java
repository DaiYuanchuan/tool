package cn.novelweb.tool.annotation.log;

import java.lang.annotation.*;

/**
 * <p>系统访问日志注解</p>
 * <p>2019-12-03 20:57</p>
 *
 * @author Dai Yuanchuan
 **/
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessLog {

    /**
     * 定义用户访问的模块名称
     *
     * @return 模块名称
     */
    String title() default "";

    /**
     * 定义是否需要获取用户IP的实际地理位置
     * 获取 IP地理位置 需要耗费一定的时间,一般平均在 75ms 左右
     * 根据实际情况选择是否需要获取
     * 获取到的 IP地理位置 仅供参考
     *
     * @return 值为true时, 同时 获取IP值、IP实际地理位置 值为false时,仅获取 IP值
     */
    boolean isGetIp() default true;

}

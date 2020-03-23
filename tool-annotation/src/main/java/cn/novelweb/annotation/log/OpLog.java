package cn.novelweb.annotation.log;

import cn.novelweb.annotation.log.pojo.FixedBusinessType;

import java.lang.annotation.*;

/**
 * <p>定义操作日志注解</p>
 * <p>2019-12-05 18:59</p>
 *
 * @author Dai Yuanchuan
 **/
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpLog {

    /**
     * 正在操作的模块名称
     *
     * @return 模块名称
     */
    String title() default "";

    /**
     * 正在操作的业务类型
     *
     * @return 业务类型
     */
    String businessType() default FixedBusinessType.OTHER;

    /**
     * 是否需要保存URL的请求参数
     * 如果请求参数过大，不建议开启
     *
     * @return 默认true:保存URL请求参数
     */
    boolean isSaveRequestData() default true;

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

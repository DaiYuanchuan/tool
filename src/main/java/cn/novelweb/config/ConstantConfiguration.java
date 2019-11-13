package cn.novelweb.config;

/**
 * @program: tool
 * @description: 基本常量配置
 * @author: Dai Yuanchuan
 * @create: 2019-10-30 02:02
 **/
public class ConstantConfiguration {

    /**
     * 常量 请求成功时 返回的状态码
     */
    public static final String SUCCESS = "0";

    /**
     * 常量 请求失败时 返回的状态码
     */
    public static final String FAIL = "1";

    /**
     * 常量 token过期时 返回的状态码
     */
    public static final String REFUSE = "403";

    /**
     * 常量 无权限异常时 返回的状态码
     */
    public static final String NO_AUTHORITY = "401";

    /**
     * 常量 服务器出错时 返回的状态码
     */
    public static final String SERVICE_ERROR = "500";

}

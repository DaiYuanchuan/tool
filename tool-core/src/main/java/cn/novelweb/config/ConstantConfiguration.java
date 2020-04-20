package cn.novelweb.config;

/**
 * <p>基本常量配置</p>
 * 2019-10-30 02:02
 *
 * @author Dai Yuanchuan
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

    /**
     * 常量 正则 url验证
     */
    public static final String URL_REGULARIZATION = "^(http|https|ftp)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";

    /**
     * 常量 正则 不含小数 数字
     */
    public static final String NUMBER_REGULARIZATION = "[0-9]*";

}
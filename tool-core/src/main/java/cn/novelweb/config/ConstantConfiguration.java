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
    public static String success = "0";

    /**
     * 常量 请求失败时 返回的状态码
     */
    public static String fail = "1";

    /**
     * 常量 token过期时 返回的状态码
     */
    public static String refuse = "403";

    /**
     * 常量 无权限异常时 返回的状态码
     */
    public static String noAuthority = "401";

    /**
     * 常量 服务器出错时 返回的状态码
     */
    public static String systemError = "500";

    /**
     * 常量 正则 url验证
     */
    public static final String URL_REGULARIZATION = "^(http|https|ftp)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";

    /**
     * 常量 最大的银行卡号长度
     */
    public static final int MAX_BANK_CARD_NUMBER_LENGTH = 19;

    /**
     * 常量 最小的银行卡号长度
     */
    public static final int MIN_BANK_CARD_NUMBER_LENGTH = 15;

    /**
     * 常量 正则 不含小数 数字
     */
    public static final String NUMBER_REGULARIZATION = "[0-9]*";

    /**
     * 数字类型常量
     */
    public interface Number {
        /**
         * 数字0
         */
        Integer ZERO = 0;
    }
}

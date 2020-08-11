package cn.novelweb.tool.send.message.aliyuncs;

/**
 * <p>阿里短信发送时通用产品域名配置</p>
 * <p>参考：https://help.aliyun.com/document_detail/101511.html?spm=a2c4g.11186623.6.613.286b3e2cDZz5bK</p>
 * <p>2020-08-11 21:22</p>
 *
 * @author Dai Yuanchuan
 **/
public enum DomainEnum {

    /**
     * 发送短信时用的域名
     */
    SEND_SMS("dysmsapi.aliyuncs.com"),

    /**
     * 消息接收时用的域名
     */
    MESSAGES_RECEIVING("dybaseapi.aliyuncs.com"),

    /**
     * 消息接收时的备用域名
     */
    MESSAGES_RECEIVING_STANDBY("1943695596114318.mns.cn-hangzhou.aliyuncs.com");

    private final String domain;

    DomainEnum(String domain) {
        this.domain = domain;
    }

    /**
     * @return 获取域名值
     */
    public String getDomain() {
        return this.domain;
    }
}

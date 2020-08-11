package cn.novelweb.tool.send.message.aliyuncs;

/**
 * <p>阿里短信发送时返回参数的语言类型配置</p>
 * <p>2020-08-11 21:44</p>
 *
 * @author Dai Yuanchuan
 **/
public enum FormatEnum {

    /**
     * 返回参数使用json格式
     */
    JSON("json"),

    /**
     * 返回参数使用xml格式
     */
    XML("xml");

    private final String format;

    FormatEnum(String format) {
        this.format = format;
    }

    /**
     * @return 获取语言类型
     */
    public String getFormat() {
        return this.format;
    }

}

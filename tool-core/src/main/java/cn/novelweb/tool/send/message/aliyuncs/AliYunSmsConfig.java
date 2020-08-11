package cn.novelweb.tool.send.message.aliyuncs;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.novelweb.tool.date.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.TimeZone;

/**
 * <p>阿里大鱼发送短信时的基本配置</p>
 * <p>2020-08-11 14:31</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ApiModel(value = "阿里大鱼发送短信配置")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AliYunSmsConfig {

    @ApiModelProperty(value = "访问者身份", required = true)
    private String accessKeyId;

    @ApiModelProperty(value = "加密签名字符串和服务器端验证签名字符串的密钥", required = true)
    private String accessKeySecret;

    @ApiModelProperty(value = "产品域名", required = true)
    private DomainEnum domain;

    @ApiModelProperty(value = "短信签名", required = true)
    private String signName;

    @ApiModelProperty(value = "API支持的电信区域代码,如短信API的值为:cn-hangzhou")
    private String regionId;

    @ApiModelProperty(value = "API的版本号,格式为 YYYY-MM-DD。取值范围：2017-05-25。", required = true)
    private String version;

    @ApiModelProperty(value = "API 的名称。[例:SendSms]", required = true)
    private String action;

    @ApiModelProperty(value = "返回参数的语言类型")
    private FormatEnum format;

    @ApiModelProperty(value = "签名方式。取值范围：HMAC-SHA1。", required = true)
    private String signatureMethod;

    @ApiModelProperty(value = "签名唯一随机数。建议每一次请求都使用不同的随机数", required = true)
    private String signatureNonce;

    @ApiModelProperty(value = "签名算法版本。取值范围：1.0", required = true)
    private String signatureVersion;

    @ApiModelProperty(value = "请求的时间戳,按照ISO8601 标准表示,并需要使用UTC时间,格式为yyyy-MM-ddTHH:mm:ssZ", required = true)
    private String timestamp;

    /**
     * 阿里大鱼短信初始化配置[注意：产品域名 domain，初始化为发送短信时用的域名]
     * 初始化完成后需要set
     * accessKeyId、accessKeySecret、signName、action
     *
     * @return 返回部分参数带有默认值的短信配置信息
     */
    public static AliYunSmsConfig init() {
        return AliYunSmsConfig.builder()
                .domain(DomainEnum.SEND_SMS)
                .regionId("cn-hangzhou")
                .version("2017-05-25")
                .format(FormatEnum.JSON)
                .signatureMethod("HMAC-SHA1")
                .signatureNonce(IdUtil.objectId())
                .signatureVersion("1.0")
                .timestamp(DateUtils.format(new DateTime(TimeZone.getTimeZone("GMT+:08:00")), "yyyy-MM-dd'T'HH:mm:ss'Z'"))
                .build();
    }
}

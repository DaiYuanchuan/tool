package cn.novelweb.tool.send.message.aliyuncs;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>阿里大鱼发送短信时的配置缩写</p>
 * <p>2020-08-12 01:35</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ApiModel(value = "阿里大鱼发送短信配置缩写")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AliYunSmsConfigAbbreviations {

    @ApiModelProperty(value = "访问者身份", required = true)
    private String accessKeyId;

    @ApiModelProperty(value = "加密签名字符串和服务器端验证签名字符串的密钥", required = true)
    private String accessKeySecret;

    @ApiModelProperty(value = "短信签名", required = true)
    private String signName;

    @ApiModelProperty(value = "需要发送的手机号", required = true)
    private String phone;

    @ApiModelProperty(value = "短信模板ID。请在控制台模板管理页面模板CODE一列查看。[例:SMS_153055065]", required = true)
    private String templateCode;

    @ApiModelProperty(value = "短信模板变量对应的实际值，JSON格式。[例:{\"code\":\"1111\"}]", required = true)
    private JSONObject templateParam;

    @ApiModelProperty(value = "外部流水扩展字段，随意填写")
    private String outId;
}

package cn.novelweb.tool.send.message.qcloudsms;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>腾讯云发送短信时的基本配置</p>
 * <p>2020-08-12 00:57</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ApiModel(value = "腾讯云发送短信配置")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TencentCloudSmsConfig {

    @ApiModelProperty(value = "短信SdkAppId在 短信控制台 添加应用后生成的实际SdkAppId", required = true)
    private String appId;

    @ApiModelProperty(value = "短信SdkAppKey", required = true)
    private String appKey;

    @ApiModelProperty(value = "手机号的国家代码[如: 86 为中国]", required = true)
    private String nationCode;

    @ApiModelProperty(value = "需要发送的手机号码", required = true)
    private String phone;

    @ApiModelProperty(value = "短信类型，0 为普通短信，1 营销短信", required = true)
    private Integer type;

    @ApiModelProperty(value = "需要发送的短信的内容,必须与申请的模板格式一致,否则将返回错误", required = true)
    private String content;

    @ApiModelProperty(value = "扩展码，可填空")
    private String extend;

    @ApiModelProperty(value = "服务端原样返回的参数，可填空")
    private String ext;

}

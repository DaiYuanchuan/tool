package cn.novelweb.tool.send.message;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpRequest;
import cn.novelweb.tool.send.message.aliyuncs.AliYunSmsConfig;
import cn.novelweb.tool.send.message.aliyuncs.GenerateAliYunSignature;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>发送短信时调用的公共接口</p>
 * <p>2020-08-11 14:28</p>
 *
 * @author Dai Yuanchuan
 **/
@Slf4j
public class SmsUtil {

    /**
     * 调用阿里大鱼进行短信发送
     *
     * @param aliYunSmsConfig 阿里大鱼短信配置
     * @param phone           需要发送的手机号
     * @param templateCode    短信模板ID。请在控制台模板管理页面模板CODE一列查看。[例:SMS_153055065]
     * @param outId           外部流水扩展字段，随意填写
     * @param smsUpExtendCode 上行短信扩展码，无特殊需要此字段的用户请忽略此字段。
     * @param templateParam   短信模板变量对应的实际值，JSON格式。[例:{"code":"1111"}]
     * @return 返回请求结果
     */
    public static String sendSms(AliYunSmsConfig aliYunSmsConfig, String phone, String templateCode, String outId, String smsUpExtendCode, JSONObject templateParam) {
        // 基础校验
        Assert.isTrue(Validator.isMobile(phone), "手机号不正确");
        Assert.notBlank(templateCode, "短信模板id为空");
        Assert.notNull(aliYunSmsConfig, "配置信息为空");
        // 发送短信时系统规定参数 取值:SendSms
        aliYunSmsConfig.setAction("SendSms");
        // 获取短信签名信息
        JSONObject aliYunSignature = GenerateAliYunSignature.getAliYunSignature(phone, templateCode, outId, smsUpExtendCode, templateParam, aliYunSmsConfig);
        Assert.isFalse(aliYunSignature.isEmpty(), "获取短信签名失败");

        // 请求最终构建的url,获取请求body
        return HttpRequest.get(aliYunSignature.getString("requestUrl")).timeout(-1).execute().body();
    }


}

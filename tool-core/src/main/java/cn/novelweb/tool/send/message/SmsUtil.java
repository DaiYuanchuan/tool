package cn.novelweb.tool.send.message;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.novelweb.tool.send.message.aliyuncs.AliYunSmsConfig;
import cn.novelweb.tool.send.message.aliyuncs.AliYunSignature;
import cn.novelweb.tool.send.message.aliyuncs.AliYunSmsConfigAbbreviations;
import cn.novelweb.tool.send.message.qcloudsms.TencentCloudSmsConfig;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Random;

/**
 * <p>发送短信时调用的工具类</p>
 * <p>此工具类包含有常用的阿里云短信操作、腾讯云短信操作</p>
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
    public static String aliYunSendSms(AliYunSmsConfig aliYunSmsConfig, String phone, String templateCode, String outId, String smsUpExtendCode, JSONObject templateParam) {
        // 基础校验
        Assert.isTrue(Validator.isMobile(phone), "手机号不正确");
        Assert.notBlank(templateCode, "短信模板id为空");
        Assert.notNull(aliYunSmsConfig, "配置信息为空");
        // 发送短信时系统规定参数 取值:SendSms
        aliYunSmsConfig.setAction("SendSms");
        // 获取短信签名信息
        JSONObject aliYunSignature = AliYunSignature.getAliYunSmsSignature(phone, templateCode, outId, smsUpExtendCode, templateParam, aliYunSmsConfig);
        Assert.isFalse(aliYunSignature.isEmpty(), "获取短信签名失败");

        // 请求最终构建的url,获取请求body
        return HttpRequest.get(aliYunSignature.getString("requestUrl")).timeout(-1).execute().body();
    }

    /**
     * 调用阿里大鱼进行短信发送
     *
     * @param aliYunSmsConfigAbbreviations 阿里大鱼发送短信配置缩写
     * @return 返回请求结果
     */
    public static String aliYunSendSms(AliYunSmsConfigAbbreviations aliYunSmsConfigAbbreviations) {
        Assert.notNull(aliYunSmsConfigAbbreviations, "配置信息为空");
        // 构建短信配置信息
        AliYunSmsConfig aliYunSmsConfig = AliYunSmsConfig.init();
        aliYunSmsConfig.setAccessKeyId(aliYunSmsConfigAbbreviations.getAccessKeyId());
        aliYunSmsConfig.setAccessKeySecret(aliYunSmsConfigAbbreviations.getAccessKeySecret());
        aliYunSmsConfig.setSignName(aliYunSmsConfigAbbreviations.getSignName());
        // 发送请求
        return aliYunSendSms(aliYunSmsConfig, aliYunSmsConfigAbbreviations.getPhone(), aliYunSmsConfigAbbreviations.getTemplateCode(),
                aliYunSmsConfigAbbreviations.getOutId(), null, aliYunSmsConfigAbbreviations.getTemplateParam());
    }

    /**
     * 调用腾讯云进行短信发送
     *
     * @param tencentCloudSmsConfig 腾讯云短信配置信息
     * @return 返回请求结果
     */
    public static String tencentCloudSendSms(TencentCloudSmsConfig tencentCloudSmsConfig) {
        Assert.notNull(tencentCloudSmsConfig, "配置信息为空");

        try {
            // 获取随机数
            long randomNumber = (new Random(DateUtil.currentSeconds())).nextLong() % 900000 + 100000;
            // 当前时间的时间戳（秒）
            long timeStamp = DateUtil.currentSeconds();


            // 构建需要发送的手机号数据
            JSONObject mobile = new JSONObject();
            // 手机号的国家代码
            mobile.put("nationcode", tencentCloudSmsConfig.getNationCode());
            mobile.put("mobile", tencentCloudSmsConfig.getPhone());

            // 构建请求body
            JSONObject requestBody = new JSONObject();
            requestBody.put("tel", mobile);
            // 短信类型，0 为普通短信，1 营销短信
            requestBody.put("type", tencentCloudSmsConfig.getType());
            // 需要发送的短信的内容[使用实际数据替换模板中的参数]
            requestBody.put("msg", tencentCloudSmsConfig.getContent());

            // 构建形成签名的字符串
            String signature = StrUtil.format("appkey={}&random={}&time={}&mobile={}",
                    tencentCloudSmsConfig.getAppKey(), randomNumber, timeStamp, tencentCloudSmsConfig.getPhone());
            // 加密签名字符串
            requestBody.put("sig", DigestUtils.sha256Hex(signature));
            requestBody.put("time", timeStamp);
            requestBody.put("extend", tencentCloudSmsConfig.getExtend());
            requestBody.put("ext", tencentCloudSmsConfig.getExt());

            // 构建请求Url
            String url = StrUtil.format("https://yun.tim.qq.com/v5/tlssmssvr/sendsms?sdkappid={}&random={}",
                    tencentCloudSmsConfig.getAppId(), randomNumber);

            // 构建一个POST的链式请求
            return HttpRequest.post(url).header("Conetent-Type", "application/json")
                    .body(requestBody.toJSONString()).timeout(-1).execute().body();
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
            Assert.isFalse(Boolean.TRUE, "所需参数为Null");
            return "NullPointerException";
        }
    }
}

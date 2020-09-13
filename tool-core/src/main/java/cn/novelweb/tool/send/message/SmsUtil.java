package cn.novelweb.tool.send.message;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.novelweb.tool.send.message.aliyuncs.AliYunSmsConfig;
import cn.novelweb.tool.send.message.aliyuncs.AliYunSignature;
import cn.novelweb.tool.send.message.aliyuncs.AliYunSmsConfigAbbreviations;
import cn.novelweb.tool.send.message.qcloudsms.TencentCloudSignature;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
     * @param phoneNumber    需要发送的手机号码[单次请求最多支持200个手机号且要求全为境内手机号或全为境外手机号,格式为+[国家或地区码][手机号] 例: +8617895721475]
     * @param templateId     模板 ID，必须填写已审核通过的模板 ID，若向境外手机号发送短信，仅支持使用国际/港澳台短信模板。
     * @param templateParam  模板参数，若无模板参数，则设置为空。
     * @param extendCode     短信码号扩展号，默认未开通
     * @param sessionContext 用户的 session 内容，可以携带用户侧 ID 等上下文信息，server 会原样返回。
     * @param senderId       国际/港澳台短信 senderid，国内短信填空，默认未开通
     * @param appId          短信SdkAppId在 短信控制台 添加应用后生成的实际SdkAppId
     * @param sign           短信签名内容，使用 UTF-8 编码，必须填写已审核通过的签名
     * @param secretId       腾讯云账户密钥对secretId
     * @param secretKey      腾讯云账户密钥对secretKey
     * @return 返回请求结果
     */
    public static String tencentCloudSendSms(JSONArray phoneNumber, String templateId, JSONArray templateParam,
                                             String extendCode, String sessionContext, String senderId,
                                             String appId, String sign, String secretId, String secretKey) {
        // 构建请求参数
        Map<String, Object> parameters = new HashMap<String, Object>(8) {{
            put("PhoneNumberSet", phoneNumber);
            put("TemplateID", templateId);
            put("TemplateParamSet", templateParam);
            put("SmsSdkAppid", appId);
            put("Sign", sign);
            put("ExtendCode", extendCode);
            put("SessionContext", sessionContext);
            put("SenderId", senderId);
        }};

        // 构建签名信息
        JSONObject signature = TencentCloudSignature.getTencentCloudSignature(parameters, "sms.tencentcloudapi.com", secretId, secretKey);

        // 构建链式请求
        return HttpRequest.post("https://sms.tencentcloudapi.com/")
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Host", "sms.tencentcloudapi.com")
                .header("Authorization", signature.getString("signature"))
                .header("X-TC-Action", "SendSms")
                .header("X-TC-Timestamp", signature.getString("timestamp"))
                .header("X-TC-Version", "2019-07-11")
                .header("X-TC-RequestClient", "SDK_JAVA_3.1.130")
                .header("X-TC-Region", "")
                .body(JSON.toJSONString(parameters).getBytes(StandardCharsets.UTF_8))
                .timeout(-1).execute().body();
    }

    /**
     * 调用腾讯云进行短信发送
     *
     * @param templateId    模板 ID，必须填写已审核通过的模板 ID，若向境外手机号发送短信，仅支持使用国际/港澳台短信模板。
     * @param templateParam 模板参数，若无模板参数，则设置为空。
     * @param appId         短信SdkAppId在 短信控制台 添加应用后生成的实际SdkAppId
     * @param sign          短信签名内容，使用 UTF-8 编码，必须填写已审核通过的签名
     * @param secretId      腾讯云账户密钥对secretId
     * @param secretKey     腾讯云账户密钥对secretKey
     * @param phoneNumber   需要发送的手机号码[单次请求最多支持200个手机号且要求全为境内手机号或全为境外手机号,格式为+[国家或地区码][手机号] 例: +8617895721475]
     * @return 返回请求结果
     */
    public static String tencentCloudSendSms(String templateId, JSONArray templateParam, String appId, String sign,
                                             String secretId, String secretKey, String... phoneNumber) {
        return tencentCloudSendSms(JSONArray.parseArray(JSONArray.toJSONString(phoneNumber)), templateId, templateParam,
                "", "", "", appId, sign, secretId, secretKey);
    }

    /**
     * 调用腾讯云进行短信发送
     *
     * @param phoneNumber 需要发送的中国手机号码[这里的国家代码默认为86]
     * @param content     需要发送的短信的内容[使用实际数据替换模板中的参数]
     * @param appId       短信SdkAppId在 短信控制台 添加应用后生成的实际SdkAppId
     * @param appKey      短信AppKey在 短信控制台 添加应用后生成的实际AppKey
     * @return 返回请求结果
     */
    public static String tencentCloudSendSms(String phoneNumber, String content, String appId, String appKey) {
        // 获取随机数
        long randomNumber = (new Random(DateUtil.currentSeconds())).nextLong() % 900000 + 100000;
        // 当前时间的时间戳（秒）
        long timeStamp = DateUtil.currentSeconds();

        // 构建需要发送的手机号数据
        JSONObject mobile = new JSONObject();
        // 手机号的国家代码
        mobile.put("nationcode", "86");
        mobile.put("mobile", phoneNumber);

        // 构建请求body
        JSONObject requestBody = new JSONObject();
        requestBody.put("tel", mobile);
        // 短信类型，0 为普通短信，1 营销短信
        requestBody.put("type", 0);
        // 需要发送的短信的内容[使用实际数据替换模板中的参数]
        requestBody.put("msg", content);

        // 构建形成签名的字符串
        String signature = StrUtil.format("appkey={}&random={}&time={}&mobile={}",
                appKey, randomNumber, timeStamp, phoneNumber);
        // 加密签名字符串
        requestBody.put("sig", DigestUtils.sha256Hex(signature));
        requestBody.put("time", timeStamp);

        // 构建请求Url
        String url = StrUtil.format("https://yun.tim.qq.com/v5/tlssmssvr/sendsms?sdkappid={}&random={}", appId, randomNumber);

        // 构建一个POST的链式请求
        return HttpRequest.post(url).header("Conetent-Type", "application/json")
                .body(requestBody.toJSONString()).timeout(-1).execute().body();
    }
}

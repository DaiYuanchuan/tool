package cn.novelweb.tool.send.message.aliyuncs;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * <p>阿里云API签名测试</p>
 * <p>获取ID、SECRET: https://ram.console.aliyun.com/manage/ak?spm=5176.12818093.nav-right.dak.488716d00nyfCh</p>
 * <p>2021-02-04 10:07</p>
 *
 * @author Dan
 **/
public class AliYunSignatureTest {

    /**
     * 访问密钥ID  用于调用API
     */
    private static final String ACCESSKEY_ID = "";

    /**
     * 密钥
     */
    private static final String ACCESSKEY_SECRET = "";

    /**
     * 短信发送
     */
    @Test
    public void sendSms() {
        JSONObject templateParam = new JSONObject();
        templateParam.put("code", "123456");
        templateParam.put("time", "5");
        // 构建请求参数
        Map<String, String> parameters = signatureParameterInitialization();
        // 第二部分由业务API参数组成
        parameters.put("Action", "SendSms");
        parameters.put("Version", "2017-05-25");
        parameters.put("RegionId", "cn-hangzhou");
        parameters.put("PhoneNumbers", "+8613844444444");
        parameters.put("SignName", "hutool");
        parameters.put("TemplateCode", "SMS_199222222");
        parameters.put("TemplateParam", templateParam.toString());
        parameters.put("OutId", "");
        parameters.put("SmsUpExtendCode", "");
        Console.log(AliYunSignature.getAliYunSignature(parameters, ACCESSKEY_SECRET, "dysmsapi.aliyuncs.com"));
    }

    /**
     * 获取云服务器信息
     */
    @Test
    public void getEcs() {
        Map<String, String> parameters = signatureParameterInitialization();
        parameters.put("Action", "DescribeInstances");
        parameters.put("Version", "2014-05-26");
        parameters.put("RegionId", "cn-hangzhou");
        Console.log(AliYunSignature.getAliYunSignature(parameters, ACCESSKEY_SECRET, "ecs.aliyuncs.com"));
    }

    /**
     * 设置安全组
     */
    @Test
    public void setSecurityGroup() {
        Map<String, String> parameters = signatureParameterInitialization();
        // 第二部分由业务API参数组成
        parameters.put("Action", "AuthorizeSecurityGroup");
        parameters.put("Version", "2014-05-26");
        parameters.put("IpProtocol", "tcp");
        parameters.put("PortRange", "8000/9600");
        parameters.put("RegionId", "cn-hangzhou");
        parameters.put("SecurityGroupId", "sg-bp17zst8hsu4rpwy2l37");
        parameters.put("SourceCidrIp", "0.0.0.0/0");
        Console.log(AliYunSignature.getAliYunSignature(parameters, ACCESSKEY_SECRET, "ecs.aliyuncs.com"));
    }

    /**
     * 获取所有存储空间（Bucket），其中正斜线（/）表示根目录
     */
    @Test
    public void getServiceListBuckets() {
        Map<String, String> parameters = signatureParameterInitialization();
        JSONObject signature = AliYunSignature.getAliYunSignature(parameters, ACCESSKEY_SECRET, "oss-cn-shanghai.aliyuncs.com");
        Console.log(signature);

        String result = HttpRequest.get(signature.getString("requestUrl"))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Host", "oss-cn-shanghai.aliyuncs.com")
                .header("Authorization", signature.getString("signature"))
                .timeout(-1).execute().body();
        Console.log(result);
    }

    // =================================================================================================================

    /**
     * 初始化签名参数
     * 组建阿里云签名常用的公共请求参数
     *
     * @return 返回一个Map，包含阿里云签名的公共请求参数
     */
    private static Map<String, String> signatureParameterInitialization() {
        // 构建请求参数
        Map<String, String> parameters = new TreeMap<>();
        // 第一部分由系统参数组成
        parameters.put("SignatureMethod", "HMAC-SHA1");
        parameters.put("SignatureNonce", IdUtil.objectId());
        parameters.put("AccessKeyId", ACCESSKEY_ID);
        parameters.put("SignatureVersion", "1.0");
        parameters.put("Timestamp", DateUtil.format(new DateTime(TimeZone.getTimeZone("GMT+:08:00")), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        parameters.put("Format", "json");
        return parameters;
    }
}

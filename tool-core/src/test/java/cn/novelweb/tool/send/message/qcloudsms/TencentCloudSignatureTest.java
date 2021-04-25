package cn.novelweb.tool.send.message.qcloudsms;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>腾讯云API签名测试</p>
 * <p>2021-02-04 10:13</p>
 *
 * @author Dan
 **/
public class TencentCloudSignatureTest {

    /**
     * 访问密钥ID  用于调用API
     */
    private static final String SECRET_ID = "";

    /**
     * 密钥
     */
    private static final String SECRET_KEY = "";

    /**
     * 短信发送
     */
    @Test
    public void sendSms() {
        // 需要发送的电话(需要+86)
        JSONArray phoneNumberSet = new JSONArray();
        phoneNumberSet.add("+8613844444444");
        phoneNumberSet.add("+8613855555555");

        // 模板参数
        JSONArray templateParam = new JSONArray();
        templateParam.add("123456");
        templateParam.add("3");

        // 构建请求参数
        Map<String, Object> parameters = new HashMap<String, Object>(8) {{
            put("PhoneNumberSet", phoneNumberSet);
            put("TemplateID", "777777");
            put("TemplateParamSet", templateParam);
            put("SmsSdkAppid", "4444444444");
            put("Sign", "hutool");
            put("ExtendCode", "");
            put("SessionContext", "");
            put("SenderId", "");
        }};

        // 构建签名信息
        JSONObject signature = TencentCloudSignature.getTencentCloudSignature(parameters, "sms.tencentcloudapi.com", SECRET_ID, SECRET_KEY);
        Console.log(signature);

        // 构建链式请求
        String result = HttpRequest.post("https://sms.tencentcloudapi.com/")
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Host", "sms.tencentcloudapi.com")
                .header("Authorization", signature.getString("signature"))
                .header("X-TC-Action", "SendSms")
                .header("X-TC-Timestamp", signature.getString("timestamp"))
                .header("X-TC-Version", "2019-07-11")
                .header("X-TC-RequestClient", "SDK_JAVA_3.1.130")
                .header("X-TC-Region", "")
                .body(JSONUtil.toJsonStr(parameters).getBytes(StandardCharsets.UTF_8))
                .timeout(-1).execute().body();
        Console.log(result);
    }

    /**
     * 获取云服务器信息
     */
    @Test
    public void getCvm() {

        // 构建请求参数
        Map<String, Object> parameters = new HashMap<String, Object>(2) {{
            // 不是必须参数
            put("Offset", 0);
            put("Limit", 20);
        }};

        // 构建签名信息
        JSONObject signature = TencentCloudSignature.getTencentCloudSignature(parameters, "cvm.tencentcloudapi.com", SECRET_ID, SECRET_KEY);
        Console.log(signature);

        String result = HttpRequest.post("https://sms.tencentcloudapi.com/")
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Host", "cvm.tencentcloudapi.com")
                .header("Authorization", signature.getString("signature"))
                .header("X-TC-Action", "DescribeInstances")
                .header("X-TC-Timestamp", signature.getString("timestamp"))
                .header("X-TC-Version", "2017-03-12")
                .header("X-TC-RequestClient", "SDK_JAVA_3.1.130")
                .header("X-TC-Region", "ap-shanghai")
                .body(JSONUtil.toJsonStr(parameters).getBytes(StandardCharsets.UTF_8))
                .timeout(-1).execute().body();
        Console.log(result);
    }

}

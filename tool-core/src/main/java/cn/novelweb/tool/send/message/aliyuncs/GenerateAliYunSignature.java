package cn.novelweb.tool.send.message.aliyuncs;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>生成阿里大鱼发送短信时的签名</p>
 * <p>2020-08-11 22:32</p>
 *
 * @author Dai Yuanchuan
 **/
public class GenerateAliYunSignature {

    /**
     * 生成阿里大鱼发送短信时需要的签名信息
     *
     * @param phone           需要发送的手机号
     * @param templateCode    短信模板ID。请在控制台模板管理页面模板CODE一列查看。[例:SMS_153055065]
     * @param outId           外部流水扩展字段。
     * @param smsUpExtendCode 上行短信扩展码，无特殊需要此字段的用户请忽略此字段。
     * @param templateParam   短信模板变量对应的实际值，JSON格式。[例:{"code":"1111"}]
     * @param aliYunSmsConfig 阿里大鱼短信配置
     * @return 返回构建的签名、参数、请求地址
     * {
     * "signature":"构建的签名信息",
     * "parameters":"请求参数信息",
     * "requestUrl":"最终拼接的url地址"
     * }
     */
    public static JSONObject getAliYunSignature(String phone, String templateCode, String outId, String smsUpExtendCode, JSONObject templateParam, AliYunSmsConfig aliYunSmsConfig) {
        // 构建请求参数
        Map<String, String> parameters = new TreeMap<String, String>() {{
            // 第一部分由系统参数组成
            put("SignatureMethod", aliYunSmsConfig.getSignatureMethod());
            put("SignatureNonce", aliYunSmsConfig.getSignatureNonce());
            put("AccessKeyId", aliYunSmsConfig.getAccessKeyId());
            put("SignatureVersion", aliYunSmsConfig.getSignatureVersion());
            put("Timestamp", aliYunSmsConfig.getTimestamp());
            put("Format", aliYunSmsConfig.getFormat().getFormat());
            // 第二部分由业务API参数组成
            put("Action", aliYunSmsConfig.getAction());
            put("Version", aliYunSmsConfig.getVersion());
            put("RegionId", aliYunSmsConfig.getRegionId());
            put("PhoneNumbers", phone);
            put("SignName", aliYunSmsConfig.getSignName());
            put("TemplateCode", templateCode);
            if (templateParam != null) {
                put("TemplateParam", templateParam.toJSONString());
            }
            if (StrUtil.isNotBlank(outId)) {
                put("OutId", outId);
            }
            if (StrUtil.isNotBlank(smsUpExtendCode)) {
                put("SmsUpExtendCode", smsUpExtendCode);
            }
        }};
        try {
            // 构造待签名的字符串
            Iterator<String> it = parameters.keySet().iterator();
            StringBuilder sortQueryStringTmp = new StringBuilder();
            while (it.hasNext()) {
                String key = it.next();
                sortQueryStringTmp.append("&").append(specialUrlEncode(key)).append("=").append(specialUrlEncode(parameters.get(key)));
            }
            // 构建签名字符串
            String stringToSign = "GET&" + specialUrlEncode("/") + "&" + specialUrlEncode(sortQueryStringTmp.substring(1));
            // 构建签名
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(StrUtil.format("{}&", aliYunSmsConfig.getAccessKeySecret()).getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = new sun.misc.BASE64Encoder().encode(signData);
            // 最终构建的签名数据
            String signature = specialUrlEncode(sign);
            JSONObject jsonObject = new JSONObject();
            // 签名最后也要做特殊URL编码
            jsonObject.put("signature", signature);
            jsonObject.put("parameters", sortQueryStringTmp);
            jsonObject.put("requestUrl", StrUtil.format("http://{}/?Signature={}{}", aliYunSmsConfig.getDomain().getDomain(), signature, sortQueryStringTmp));
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    /**
     * 构造待签名的请求串
     * 一个特殊的URL编码这个是POP特殊的一种规则
     * 即在一般的URLEncode后再增加三种字符替换：加号 （+）替换成 %20、星号 （*）替换成 %2A、 %7E 替换回波浪号 （~）参考代码如下
     *
     * @param value 需要构造的url值
     * @return 返回构造结果
     * @throws Exception 抛出异常
     */
    private static String specialUrlEncode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }
}

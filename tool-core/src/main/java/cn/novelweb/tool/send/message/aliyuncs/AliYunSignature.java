package cn.novelweb.tool.send.message.aliyuncs;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>生成阿里云API操作的签名信息</p>
 * <p>2020-08-11 22:32</p>
 *
 * @author Dai Yuanchuan
 **/
public class AliYunSignature {

    /**
     * 生成阿里云API请求签名信息
     *
     * @param parameters      参与签名构建的参数信息
     * @param accessKeySecret 加密签名字符串和服务器端验证签名字符串的密钥
     * @param domain          产品域名
     * @return 返回构建的签名、参数、请求地址
     * {
     * "signature":"构建的签名信息",
     * "parameters":"请求参数信息",
     * "requestUrl":"最终拼接的url地址"
     * }
     */
    public static JSONObject getAliYunSignature(Map<String, String> parameters, String accessKeySecret, String domain) {

        // 过滤Map中为空的参数
        parameters = MapUtil.filter(parameters, (Filter<Map.Entry<String, String>>) o -> StrUtil.isNotBlank(o.getValue()));

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
            mac.init(new SecretKeySpec(StrUtil.format("{}&", accessKeySecret).getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = new sun.misc.BASE64Encoder().encode(signData);
            // 最终构建的签名数据
            String signature = specialUrlEncode(sign);
            JSONObject jsonObject = new JSONObject();
            // 签名最后也要做特殊URL编码
            jsonObject.put("signature", signature);
            jsonObject.put("parameters", sortQueryStringTmp);
            jsonObject.put("requestUrl", StrUtil.format("http://{}/?Signature={}{}", domain, signature, sortQueryStringTmp));
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

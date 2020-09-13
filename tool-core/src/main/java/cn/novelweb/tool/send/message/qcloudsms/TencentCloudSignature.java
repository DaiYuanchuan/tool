package cn.novelweb.tool.send.message.qcloudsms;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.novelweb.tool.date.DateUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * <p>构建腾讯云签名</p>
 * <p>2020-09-13 22:33</p>
 *
 * @author Dai Yuanchuan
 **/
public class TencentCloudSignature {

    /**
     * 生成腾讯云API请求签名信息
     *
     * @param parameters 参与签名构建的参数信息
     * @param endpoint   请求域名信息 [例: sms.tencentcloudapi.com]
     * @param secretId   腾讯云账户密钥对secretId
     * @param secretKey  腾讯云账户密钥对secretKey
     * @return 返回构建的签名、参数、时间戳
     * {
     * "signature":"构建的签名信息",
     * "timestamp":"生成签名时的时间秒数"
     * }
     */
    public static JSONObject getTencentCloudSignature(Map<String, Object> parameters, String endpoint, String secretId, String secretKey) {
        // 拼接规范请求串
        String canonicalRequest = StrUtil.format("POST\n/\n\n{}\ncontent-type;host\n{}",
                StrUtil.format("content-type:application/json; charset=utf-8\nhost:{}\n", endpoint),
                sha256Hex(JSON.toJSONString(parameters)));

        // 计算时间
        String timestamp = String.valueOf(DateUtil.currentSeconds());
        String date = DateUtils.format(new DateTime(TimeZone.getTimeZone("UTC")), "yyyy-MM-dd");
        String credentialScope = StrUtil.format("{}/{}/tc3_request", date, endpoint.split("\\.")[0]);
        String stringToSign = StrUtil.format("TC3-HMAC-SHA256\n{}\n{}\n{}",
                timestamp, credentialScope,
                sha256Hex(canonicalRequest));

        // 计算签名
        byte[] secretDate = hmac256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac256(secretDate, endpoint.split("\\.")[0]);
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        String authorization = StrUtil.format("TC3-HMAC-SHA256 Credential={}/{}, SignedHeaders=content-type;host, Signature={}",
                secretId, credentialScope,
                DatatypeConverter.printHexBinary(hmac256(secretSigning, stringToSign)).toLowerCase());

        // 构建返回信息
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("signature", authorization);
        jsonObject.put("timestamp", timestamp);
        return jsonObject;
    }

    private static byte[] hmac256(byte[] key, String msg) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
            mac.init(secretKeySpec);
            return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(d).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}

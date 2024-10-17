package cn.bossfriday.im.common.helper;

import cn.bossfriday.common.utils.HashUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * ApiHelper
 *
 * @author chenx
 */
public class ApiHelper {

    public ApiHelper() {
        // do nothing
    }

    /**
     * getSignature: API签名
     *
     * @param appSecret
     * @param nonce
     * @param timestamp
     * @return
     */
    public static String getSignature(String appSecret, String nonce, String timestamp) {
        StringBuilder toSign = new StringBuilder(appSecret).append(nonce).append(timestamp);
        return HashUtils.sha1(toSign.toString());
    }

    /**
     * getSignatureHeaderMap
     *
     * @param appKey
     * @param appSecret
     * @return
     */
    public static Map<String, String> getSignatureHeaderMap(String appKey, String appSecret) {
        String nonce = String.valueOf(Math.random() * 1000000);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = getSignature(appSecret, nonce, timestamp);

        Map<String, String> map = new HashMap<>();
        map.put("AppKey", appKey);
        map.put("Nonce", nonce);
        map.put("Timestamp", timestamp);
        map.put("Signature", sign);

        return map;
    }
}

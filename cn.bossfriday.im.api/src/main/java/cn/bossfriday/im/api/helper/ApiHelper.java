package cn.bossfriday.im.api.helper;

import cn.bossfriday.common.utils.HashUtils;
import cn.bossfriday.im.common.conf.entity.AppInfo;
import cn.bossfriday.im.common.helper.AppHelper;
import cn.bossfriday.im.common.result.ResultCode;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ApiHelper
 *
 * @author chenx
 */
public class ApiHelper {

    public static final String HEADER_APP_KEY = "AppKey";
    public static final String HEADER_NONCE = "Nonce";
    public static final String HEADER_TIMESTAMP = "Timestamp";
    public static final String HEADER_SIGNATURE = "Signature";

    private ApiHelper() {
        // do nothing
    }

    /**
     * auth
     */
    public static ResultCode auth(FullHttpRequest httpRequest) {
        if (!httpRequest.headers().contains(HEADER_APP_KEY)
                || !httpRequest.headers().contains(HEADER_NONCE)
                || !httpRequest.headers().contains(HEADER_TIMESTAMP)
                || !httpRequest.headers().contains(HEADER_SIGNATURE)
        ) {
            return ResultCode.API_AUTHENTICATION_FAILED;
        }

        String appKey = httpRequest.headers().get(HEADER_APP_KEY);
        String nonce = httpRequest.headers().get(HEADER_NONCE);
        String timestamp = httpRequest.headers().get(HEADER_TIMESTAMP);
        String signature = httpRequest.headers().get(HEADER_SIGNATURE);

        AppInfo appInfo = AppHelper.getAppInfo(appKey);
        if (Objects.isNull(appInfo)) {
            return ResultCode.APP_NOT_EXISTED_OR_INVALID;
        }

        if (signature.equalsIgnoreCase(getSignature(appInfo.getAppSecret(), nonce, timestamp))) {
            return ResultCode.API_AUTHENTICATION_FAILED;
        }

        return ResultCode.OK;
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
        map.put(HEADER_APP_KEY, appKey);
        map.put(HEADER_NONCE, nonce);
        map.put(HEADER_TIMESTAMP, timestamp);
        map.put(HEADER_SIGNATURE, sign);

        return map;
    }
}

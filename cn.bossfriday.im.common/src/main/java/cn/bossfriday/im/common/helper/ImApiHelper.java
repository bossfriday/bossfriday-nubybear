package cn.bossfriday.im.common.helper;

import cn.bossfriday.common.utils.HexUtils;

/**
 * ImApiHelper
 *
 * @author chenx
 */
public class ImApiHelper {

    public ImApiHelper() {
        // do nothing
    }

    /**
     * getSignature: API签名
     *
     * @param appKey
     * @param appSecret
     * @param nonce
     * @param timestamp
     * @return
     */
    public static String getSignature(String appKey, String appSecret, String nonce, String timestamp) {
        StringBuilder toSign = new StringBuilder(appSecret).append(nonce).append(timestamp);
        return HexUtils.hexSHA1(toSign.toString());
    }
}

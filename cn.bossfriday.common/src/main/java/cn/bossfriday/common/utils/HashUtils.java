package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * HashUtils
 *
 * @author chenx
 */
public class HashUtils {

    private static final String HASH_ALGORITHM_SHA1 = "SHA-1";
    private static final String HASH_ALGORITHM_MD5 = "MD5";

    private HashUtils() {
        // do nothing
    }

    /**
     * sha1
     *
     * @param input
     * @return
     */
    public static String sha1(String input) {
        return hexHash(input, HASH_ALGORITHM_SHA1);
    }

    /**
     * md5
     *
     * @param input
     * @return
     */
    public static String md5(String input) {
        return hexHash(input, HASH_ALGORITHM_MD5);
    }

    /**
     * hexHash
     *
     * @param value
     * @return
     */
    public static String hexHash(String value, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(value.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            
            return byteToHexString(digest);
        } catch (Exception ex) {
            throw new ServiceRuntimeException(ex.getMessage());
        }
    }

    /**
     * byteToHexString
     *
     * @param bytes
     * @return
     */
    public static String byteToHexString(byte[] bytes) {
        return String.valueOf(Hex.encodeHex(bytes));
    }
}

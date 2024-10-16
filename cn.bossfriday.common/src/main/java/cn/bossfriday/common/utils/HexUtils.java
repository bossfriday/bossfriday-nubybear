package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * HexUtils
 *
 * @author chenx
 */
public class HexUtils {

    private HexUtils() {
        // do nothing
    }

    /**
     * hexSHA1
     *
     * @param value
     * @return
     */
    public static String hexSHA1(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
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

package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.ServiceRuntimeException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * EncryptUtil
 *
 * @author chenx
 */
public class EncryptUtil {

    /**
     * DES加解密时只能处理 8 字节的密钥
     */
    private static final String DES_TRANSFORMATION = "DES/ECB/PKCS5Padding";
    private static final String DES_ALGORITHM = "DES";

    /**
     * AES 支持 128 位（16 字节）、192 位（24 字节）和 256 位（32 字节）的密钥
     */
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String AES_ALGORITHM = "AES";

    private EncryptUtil() {
        // do nothing
    }

    /**
     * desEncrypt
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] desEncrypt(byte[] data, byte[] key) {
        return encrypt(data, key, DES_TRANSFORMATION, DES_ALGORITHM);
    }

    /**
     * desDecrypt
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] desDecrypt(byte[] data, byte[] key) {
        return decrypt(data, key, DES_TRANSFORMATION, DES_ALGORITHM);
    }

    /**
     * aesEncrypt：
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] aesEncrypt(byte[] data, byte[] key) {
        return encrypt(data, key, AES_TRANSFORMATION, AES_ALGORITHM);
    }

    /**
     * aesDecrypt
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] aesDecrypt(byte[] data, byte[] key) {
        return decrypt(data, key, AES_TRANSFORMATION, AES_ALGORITHM);
    }

    /**
     * decrypt
     */
    private static byte[] decrypt(byte[] data, byte[] key, String transformation, String algorithm) {
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            return cipher.doFinal(data);
        } catch (Exception ex) {
            throw new ServiceRuntimeException(algorithm + " decrypt error! msg: " + ex.getMessage());
        }
    }

    /**
     * encrypt
     */
    private static byte[] encrypt(byte[] data, byte[] key, String transformation, String algorithm) {
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            return cipher.doFinal(data);
        } catch (Exception ex) {
            throw new ServiceRuntimeException(algorithm + "encrypt error! msg: " + ex.getMessage());
        }
    }
}

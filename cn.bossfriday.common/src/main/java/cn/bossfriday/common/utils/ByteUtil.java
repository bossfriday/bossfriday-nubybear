package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * ByteUtil
 *
 * @author chenx
 */
public class ByteUtil {

    public static final int MAX_VALUE_UNSIGNED_INT8 = 255;
    public static final int MAX_VALUE_UNSIGNED_INT16 = 65535;
    public static final int MAX_VALUE_UNSIGNED_INT24 = 16777215;

    public static final int INT_16_BYTES_LENGTH = 2;
    public static final int INT_24_BYTES_LENGTH = 3;
    public static final int INT_48_BYTES_LENGTH = 6;
    public static final int INT_BYTES_LENGTH = 4;
    public static final int LONG_BYTES_LENGTH = 8;

    private ByteUtil() {

    }

    /**
     * bytesToUnsignedInt16
     *
     * @param bytes
     * @return
     * @throws ServiceRuntimeException
     */
    public static int bytesToUnsignedInt16(byte[] bytes) {
        if (bytes.length != INT_16_BYTES_LENGTH) {
            throw new ServiceRuntimeException("bytes.length must be 2.");
        }

        return bytes[0] << 8 & 0xFF00 | bytes[1] & 0xFF;
    }

    /**
     * unsignedInt16ToBytes
     *
     * @param value
     * @return
     */
    public static byte[] unsignedInt16ToBytes(int value) {
        byte[] bytes = new byte[INT_16_BYTES_LENGTH];
        bytes[0] = (byte) ((value & 0xFF00) >> 8);
        bytes[1] = (byte) (value & 0xFF);

        return bytes;
    }

    /**
     * bytesToUnsignedInt24
     *
     * @param bytes
     * @return
     * @throws ServiceRuntimeException
     */
    public static int bytesToUnsignedInt24(byte[] bytes) {
        if (bytes.length != INT_24_BYTES_LENGTH) {
            throw new ServiceRuntimeException("bytes.length must be 3.");
        }

        return (bytes[2] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[0] & 0x0F) << 16);
    }

    /**
     * unsignedInt24ToBytes
     *
     * @param value
     * @return
     */
    public static byte[] unsignedInt24ToBytes(int value) {
        byte[] bytes = new byte[INT_24_BYTES_LENGTH];
        bytes[0] = (byte) ((value >> 16) & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[2] = (byte) (value & 0xFF);

        return bytes;
    }

    /**
     * ipToInt
     *
     * @param ip
     * @return
     */
    public static int ipToInt(String ip) {
        byte[] addr = ipToBytes(ip);
        // reference java.net.Inet4Address.Inet4Address
        int address = addr[3] & 0xFF;
        address |= ((addr[2] << 8) & 0xFF00);
        address |= ((addr[1] << 16) & 0xFF0000);
        address |= ((addr[0] << 24) & 0xFF000000);

        return address;
    }

    /**
     * intToIp
     *
     * @param ip
     * @return
     */
    public static String intToIp(int ip) {
        byte[] bytes = new byte[INT_BYTES_LENGTH];
        bytes[0] = (byte) ((ip >>> 24) & 0xFF);
        bytes[1] = (byte) ((ip >>> 16) & 0xFF);
        bytes[2] = (byte) ((ip >>> 8) & 0xFF);
        bytes[3] = (byte) (ip & 0xFF);

        return bytesToIp(bytes);
    }

    /**
     * bytesToIp
     *
     * @param src
     * @return
     */
    public static String bytesToIp(byte[] src) {
        return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + "." + (src[3] & 0xff);
    }

    /**
     * ipToBytes
     *
     * @param ip
     * @return
     */
    public static byte[] ipToBytes(String ip) {
        byte[] bytes = new byte[INT_BYTES_LENGTH];
        String[] strings = StringUtils.split(ip, ".");
        for (int i = 0; i < strings.length; i++) {
            bytes[i] = (byte) Integer.parseInt(strings[i]);
        }

        return bytes;
    }

    /**
     * string2Bytes
     *
     * @param str
     * @return
     * @throws ServiceRuntimeException
     */
    public static byte[] string2Bytes(String str) {
        if (StringUtils.isEmpty(str)) {
            throw new ServiceRuntimeException("input string is empty!");
        }

        return str.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * bytes2String
     *
     * @param bytes
     * @return
     * @throws ServiceRuntimeException
     */
    public static String bytes2String(byte[] bytes) {
        if (bytes == null) {
            throw new ServiceRuntimeException("input bytes is null!");
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * substring
     *
     * @param bytes
     * @param startIndex
     * @param len
     * @return
     */
    public static byte[] substring(byte[] bytes, int startIndex, int len) {
        byte[] subBytes = new byte[len];
        System.arraycopy(bytes, startIndex, subBytes, 0, len);

        return subBytes;
    }

    /**
     * int2Bytes
     *
     * @param num
     * @return
     */
    public static byte[] int2Bytes(int num) {
        byte[] byteNum = new byte[INT_BYTES_LENGTH];
        for (int ix = 0; ix < INT_BYTES_LENGTH; ++ix) {
            int offset = 32 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }

        return byteNum;
    }

    /**
     * bytes2Int
     *
     * @param byteNum
     * @return
     */
    public static int bytes2Int(byte[] byteNum) {
        int num = 0;
        for (int ix = 0; ix < INT_BYTES_LENGTH; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }

        return num;
    }

    /**
     * long2Bytes
     *
     * @param num
     * @return
     */
    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[LONG_BYTES_LENGTH];
        for (int ix = 0; ix < LONG_BYTES_LENGTH; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }

        return byteNum;
    }

    /**
     * bytes2Long
     *
     * @param byteNum
     * @return
     */
    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < LONG_BYTES_LENGTH; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }

        return num;
    }

    /**
     * number482Bytes
     *
     * @param num
     * @return
     */
    public static byte[] number482Bytes(long num) {
        byte[] byteNum = new byte[INT_48_BYTES_LENGTH];
        for (int ix = 0; ix < INT_48_BYTES_LENGTH; ++ix) {
            int offset = 48 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }

        return byteNum;
    }

    /**
     * bytes2number48
     *
     * @param byteNum
     * @return
     */
    public static long bytes2number48(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < INT_48_BYTES_LENGTH; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }

        return num;
    }
}

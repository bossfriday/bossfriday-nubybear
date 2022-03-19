package cn.bossfriday.common.utils;

import org.apache.commons.lang.StringUtils;

import java.net.UnknownHostException;

public class ByteUtil {
    private static final String CHARSET = "UTF-8";

    /**
     * convert ip string to int
     *
     * @param ip
     * @return int
     * @throws UnknownHostException
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
     * convert int to ip string
     *
     * @param ip
     * @return xxx.xxx.xxx.xxx
     */
    public static String intToIp(int ip) {
        byte[] addr = new byte[4];
        addr[0] = (byte) ((ip >>> 24) & 0xFF);
        addr[1] = (byte) ((ip >>> 16) & 0xFF);
        addr[2] = (byte) ((ip >>> 8) & 0xFF);
        addr[3] = (byte) (ip & 0xFF);

        return bytesToIp(addr);
    }

    /**
     * convert byte array to ip string
     *
     * @param src
     * @return xxx.xxx.xxx.xxx
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
        byte[] binIP = new byte[4];
        String[] strs = StringUtils.split(ip, ".");
        for (int i = 0; i < strs.length; i++) {
            binIP[i] = (byte) Integer.parseInt(strs[i]);
        }

        return binIP;
    }

    /**
     * stringToBytes
     *
     * @param str
     * @return
     */
    public static byte[] string2Bytes(String str) throws Exception {
        if (StringUtils.isEmpty(str))
            throw new Exception("input string is empty!");

        return str.getBytes(CHARSET);
    }

    /**
     * toString
     *
     * @param bytes
     * @return
     */
    public static String bytes2String(byte[] bytes) throws Exception {
        if (bytes == null)
            throw new Exception("input bytes is null!");

        return new String(bytes, CHARSET);
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
     * bytes2Int
     *
     * @param bytes
     * @return
     */
    public static int bytes2Int(byte[] bytes) {
        int value;
        value = (int) (((bytes[0] & 0xFF) << 24)
                | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8)
                | (bytes[3] & 0xFF));

        return value;
    }

    /**
     * @param value
     * @return
     */
    public static byte[] int2Bytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);

        return src;
    }
}

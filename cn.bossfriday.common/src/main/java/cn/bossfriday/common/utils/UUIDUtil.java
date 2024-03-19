package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.ServiceRuntimeException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * UUIDUtil
 *
 * @author chenx
 */
public class UUIDUtil {

    private static final char[] DIGITS64 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_".toCharArray();
    private static final int UUID_SIGNIFICANT_BYTES_LENGTH = 8;
    private static final int UUID_BYTES_LENGTH = 16;

    private UUIDUtil() {

    }

    /**
     * getUUID
     */
    public static UUID getUuid() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong());
    }

    /**
     * getUUID
     *
     * @param msb
     * @param lsb
     * @return
     */
    public static UUID getUUID(long msb, long lsb) {
        return new UUID(msb, lsb);
    }

    /**
     * getUuidBytes
     *
     * @return
     */
    public static byte[] getUuidBytes() {
        return toBytes(getUuid());
    }

    /**
     * toBytes
     *
     * @param uuid
     * @return
     */
    public static byte[] toBytes(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[UUID_BYTES_LENGTH];

        for (int i = 0; i < UUID_SIGNIFICANT_BYTES_LENGTH; i++) {
            buffer[i] = (byte) ((msb >>> 8 * (7 - i)) & 0xFF);
            buffer[i + 8] = (byte) ((lsb >>> 8 * (7 - i)) & 0xFF);
        }
        return buffer;
    }

    /**
     * getShortString
     *
     * @return
     */
    public static String getShortString() {
        return getShortString((getUuid()));
    }

    /**
     * getShortString
     *
     * @param u
     * @return
     */
    public static String getShortString(UUID u) {
        if (u == null) {
            u = getUuid();
        }

        return toIdString(u.getMostSignificantBits()) + toIdString(u.getLeastSignificantBits());
    }

    /**
     * getShortString
     *
     * @param bytes
     * @return
     */
    public static String getShortString(byte[] bytes) {
        UUID u = parseUuid(bytes);
        return getShortString(u);
    }

    /**
     * toIdString
     *
     * @param l
     * @return
     */
    private static String toIdString(long l) {
        // 限定11位长度
        char[] buf = "00000000000".toCharArray();
        int length = 11;
        // 0x0000003FL 00111111
        do {
            long least = 63L;
            // l & least取低6位
            buf[--length] = DIGITS64[(int) (l & least)];
            l >>>= 6;
        } while (l != 0);

        return new String(buf);
    }

    /**
     * parseUuid
     *
     * @param data
     * @return
     */
    public static UUID parseUuid(byte[] data) {
        long msb = 0;
        long lsb = 0;

        if (data.length != UUID_BYTES_LENGTH) {
            throw new ServiceRuntimeException("data must be 16 bytes in length");
        }

        for (int i = 0; i < UUID_SIGNIFICANT_BYTES_LENGTH; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }

        for (int i = UUID_SIGNIFICANT_BYTES_LENGTH; i < UUID_BYTES_LENGTH; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }

        return new UUID(msb, lsb);
    }
}

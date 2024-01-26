package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * MurmurHashUtil
 *
 * @author chenx
 */
public class MurmurHashUtil {

    private static final int INT_BYTE_LENGTH = 4;
    private static final int LONG_BYTE_LENGTH = 8;

    private MurmurHashUtil() {

    }

    /**
     * hash64
     *
     * @param key
     * @return
     * @throws ServiceRuntimeException
     * @throws UnsupportedEncodingException
     */
    public static long hash64(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new ServiceRuntimeException("input key is null or empty!");
        }

        return hash64(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * hash64
     *
     * @param key
     * @return
     */
    public static long hash64(byte[] key) {
        return hash64A(key, 0x1234ABCD);
    }

    /**
     * hash32
     *
     * @param key
     * @return
     * @throws ServiceRuntimeException
     * @throws UnsupportedEncodingException
     */
    public static int hash32(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new ServiceRuntimeException("input key is null or empty!");
        }

        return hash(key.getBytes(StandardCharsets.UTF_8), 0x1234ABCD);
    }

    /**
     * hash32
     *
     * @param key
     * @return
     */
    public static int hash32(byte[] key) {
        return hash(key, 0x1234ABCD);
    }

    /**
     * Hashes bytes in an array.
     *
     * @param data The bytes to hash.
     * @param seed The seed for the hash.
     * @return The 32 bit hash of the bytes in question.
     */
    public static int hash(byte[] data, int seed) {
        return hash(ByteBuffer.wrap(data), seed);
    }

    /**
     * Hashes bytes in part of an array.
     *
     * @param data   The data to hash.
     * @param offset Where to start munging.
     * @param length How many bytes to process.
     * @param seed   The seed to start with.
     * @return The 32-bit hash of the data in question.
     */
    public static int hash(byte[] data, int offset, int length, int seed) {
        return hash(ByteBuffer.wrap(data, offset, length), seed);
    }

    /**
     * Hashes the bytes in a buffer from the current position to the limit.
     *
     * @param buf  The bytes to hash.
     * @param seed The seed for the hash.
     * @return The 32 bit murmur hash of the bytes in the buffer.
     */
    public static int hash(ByteBuffer buf, int seed) {
        // save byte order for later restoration
        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        int m = 0x5bd1e995;
        int r = 24;

        int h = seed ^ buf.remaining();

        int k;
        while (buf.remaining() >= INT_BYTE_LENGTH) {
            k = buf.getInt();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h *= m;
            h ^= k;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(INT_BYTE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            finish.put(buf).rewind();
            h ^= finish.getInt();
            h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        buf.order(byteOrder);
        return h;
    }

    /**
     * hash64A
     *
     * @param data
     * @param seed
     * @return
     */
    public static long hash64A(byte[] data, int seed) {
        return hash64A(ByteBuffer.wrap(data), seed);
    }

    /**
     * hash64A
     *
     * @param data
     * @param offset
     * @param length
     * @param seed
     * @return
     */
    public static long hash64A(byte[] data, int offset, int length, int seed) {
        return hash64A(ByteBuffer.wrap(data, offset, length), seed);
    }

    /**
     * hash64A
     *
     * @param buf
     * @param seed
     * @return
     */
    public static long hash64A(ByteBuffer buf, int seed) {
        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long m = 0xc6a4a7935bd1e995L;
        int r = 47;

        long h = seed ^ (buf.remaining() * m);

        long k;
        while (buf.remaining() >= LONG_BYTE_LENGTH) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(LONG_BYTE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);

        return h;
    }
}

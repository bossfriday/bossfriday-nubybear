package cn.bossfriday.common.id;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.ByteUtil;
import cn.bossfriday.common.utils.MurmurHashUtil;

import java.io.*;
import java.util.Base64;

/**
 * SystemIdWorker
 *
 * @author chenx
 */
public class SystemIdWorker {

    private static SnowFlakeIdWorker snowFlakeIdWorker = new SnowFlakeIdWorker(0, 0);

    private SystemIdWorker() {
        // do nothing
    }

    /**
     * nextId
     *
     * @return
     */
    public static Long nextId() {
        return snowFlakeIdWorker.nextId();
    }

    /**
     * nextKey
     *
     * @return
     * @throws IOException
     */
    public static String nextKey() {
        byte[] bytes = serialize(snowFlakeIdWorker.nextId());
        ByteUtil.hashObfuscate(bytes, Integer.BYTES);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * getSnowFlakeId
     *
     * @param key
     * @return
     * @throws IOException
     */
    public static Long getId(String key) {
        byte[] bytes = Base64.getUrlDecoder().decode(key);
        ByteUtil.hashObfuscate(bytes, Integer.BYTES);

        return deserialize(bytes);
    }

    /**
     * getKey
     *
     * @param id
     * @return
     * @throws IOException
     */
    public static String getKey(Long id) {
        byte[] bytes = serialize(id);
        ByteUtil.hashObfuscate(bytes, Integer.BYTES);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * serialize
     *
     * @param snowflakeId
     * @return
     * @throws IOException
     */
    private static byte[] serialize(Long snowflakeId) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(out)
        ) {
            dos.writeInt(getHashCode(snowflakeId));
            dos.writeLong(snowflakeId);

            return out.toByteArray();
        } catch (IOException ex) {
            throw new ServiceRuntimeException("SystemIdWorker.serialize() failed!");
        }
    }

    /**
     * deserialize
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    private static Long deserialize(byte[] bytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(in)
        ) {
            dis.readInt();

            return dis.readLong();
        } catch (IOException ex) {
            throw new ServiceRuntimeException("SystemIdWorker.deserialize() failed!");
        }
    }

    /**
     * getHashCode
     *
     * @param snowflakeId
     * @return
     */
    private static int getHashCode(Long snowflakeId) {
        return MurmurHashUtil.hash32(ByteUtil.long2Bytes(snowflakeId));
    }
}

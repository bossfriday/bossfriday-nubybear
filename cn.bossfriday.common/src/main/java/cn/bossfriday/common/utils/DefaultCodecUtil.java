package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.BizException;

import java.io.*;

/**
 * DefaultCodecUtil
 *
 * @author chenx
 */
public class DefaultCodecUtil {

    private DefaultCodecUtil() {

    }

    /**
     * encode
     *
     * @param obj
     * @return
     * @throws BizException
     * @throws IOException
     */
    public static byte[] encode(Object obj) throws BizException, IOException {
        if (obj != null) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(obj);
                oos.flush();

                return bos.toByteArray();
            }
        }

        throw new BizException("DefaultCodecUtil.encode() error!");
    }

    /**
     * decode
     *
     * @param bytes
     * @return
     * @throws BizException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object decode(byte[] bytes) throws BizException, IOException, ClassNotFoundException {
        if (bytes != null && bytes.length > 0) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {

                return ois.readObject();
            }
        }

        throw new BizException("DefaultCodecUtil.decode() error!");
    }
}

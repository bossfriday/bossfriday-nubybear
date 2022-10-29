package cn.bossfriday.common.utils;

import cn.bossfriday.common.exception.BizException;
import org.apache.commons.lang.ArrayUtils;

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
     * @throws IOException
     */
    public static byte[] encode(Object obj) throws IOException {
        if (obj == null) {
            throw new BizException("The input object is null!");
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();

            return bos.toByteArray();
        }
    }

    /**
     * decode
     *
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object decode(byte[] bytes) throws IOException, ClassNotFoundException {
        if (ArrayUtils.isEmpty(bytes)) {
            throw new BizException("The input bytes is empty!");
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            return ois.readObject();
        }
    }
}

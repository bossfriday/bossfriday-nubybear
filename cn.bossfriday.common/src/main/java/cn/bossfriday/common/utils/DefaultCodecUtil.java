package cn.bossfriday.common.utils;

import cn.bossfriday.common.rpc.exception.SysException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DefaultCodecUtil {
    /**
     * encode
     */
    public static byte[] encode(Object obj) throws SysException {
        if (obj != null) {
            ByteArrayOutputStream bos = null;
            ObjectOutputStream oos = null;
            try {
                bos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(bos);
                oos.writeObject(obj);
                oos.flush();
                byte[] ret = bos.toByteArray();

                return ret;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (oos != null)
                        oos.close();
                    if (bos != null)
                        bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        throw new SysException("Encode error!");
    }

    /**
     * decode（default decode）
     */
    public static Object decode(byte[] bytes) throws SysException {
        if (bytes != null && bytes.length > 0) {
            ByteArrayInputStream bis = null;
            ObjectInputStream ois = null;
            try {
                bis = new ByteArrayInputStream(bytes);
                ois = new ObjectInputStream(bis);
                Object ret = ois.readObject();
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ois != null)
                        ois.close();
                    if (bis != null)
                        bis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        throw new SysException("Decode error!");
    }
}

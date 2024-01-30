package cn.bossfriday.common.rpc.actor;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.ProtostuffCodecUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * ActorMsgPayloadCodec
 *
 * @author chenx
 */
@Slf4j
public class ActorMsgPayloadCodec {

    private ActorMsgPayloadCodec() {

    }

    /**
     * encode
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] encode(T obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        try {
            out.writeUTF(obj.getClass().getName());
            out.write(ProtostuffCodecUtil.serialize(obj));

            return bos.toByteArray();
        } catch (Exception e) {
            log.error("ActorMsgPayloadCodec.encode() error!", e);
        } finally {
            try {
                out.close();
                bos.close();
            } catch (Exception e) {
                log.error("ActorMsgCodec.protoStuffEncode() close error!", e);
            }
        }

        return new byte[0];
    }

    /**
     * decode
     *
     * @param bytes
     * @param <T>
     * @return
     */
    public static <T> T decode(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bis);
        try {
            String className = in.readUTF();
            Class<T> clazz = (Class<T>) Class.forName(className);

            int available = in.available();
            byte[] data = new byte[available];
            int read = in.read(data);
            if (read <= 0) {
                throw new ServiceRuntimeException("read 0 byte!");
            }

            return ProtostuffCodecUtil.deserialize(data, clazz);
        } catch (Exception e) {
            log.error("ActorMsgPayloadCodec.decode() error!", e);
        } finally {
            try {
                in.close();
                bis.close();
            } catch (Exception e) {
                log.error("ActorMsgCodec.protoStuffDecode() close error!", e);
            }
        }

        return null;
    }
}

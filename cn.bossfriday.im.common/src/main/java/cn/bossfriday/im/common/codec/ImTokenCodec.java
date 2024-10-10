package cn.bossfriday.im.common.codec;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.Base58Util;
import cn.bossfriday.common.utils.EncryptUtil;
import cn.bossfriday.common.utils.ProtostuffCodecUtil;
import cn.bossfriday.im.common.entity.ImToken;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * ImTokenCodec
 *
 * @author chenx
 */
public class ImTokenCodec {

    private static final byte OBFUSCATE_BYTE = 0x64;
    private static final String OBFUSCATION_STRING = "BossFriday";
    private static final byte[] CORE_KEY = "chenx_NB".getBytes(StandardCharsets.UTF_8);

    private ImTokenCodec() {
        // do nothing
    }

    /**
     * encode
     *
     * @param token
     * @return
     */
    public static String encode(ImToken token) {
        try {
            byte[] data = serialize(token);
            for (int i = 0; i < data.length; i++) {
                data[i] ^= OBFUSCATE_BYTE;
            }

            return Base58Util.encode(data);
        } catch (Exception ex) {
            // ignore
        }

        return null;
    }

    /**
     * decode
     *
     * @param token
     * @return
     */
    public static ImToken decode(String token) {
        try {
            if (StringUtils.isEmpty(token)) {
                return null;
            }

            byte[] data = Base58Util.decode(token);
            for (int i = 0; i < data.length; i++) {
                data[i] ^= OBFUSCATE_BYTE;
            }

            return deserialize(data);
        } catch (Exception ex) {
            // ignore
        }

        return null;
    }

    /**
     * serialize
     *
     * @param imToken
     * @return
     * @throws IOException
     */
    private static byte[] serialize(ImToken imToken) throws IOException {
        if (Objects.isNull(imToken)) {
            throw new ServiceRuntimeException("imToken is null!");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream os = new DataOutputStream(out)
        ) {
            os.writeLong(imToken.getAppId());
            os.writeInt(CORE_KEY.length);
            os.writeUTF(OBFUSCATION_STRING);
            os.write(CORE_KEY);

            byte[] payload = ProtostuffCodecUtil.serialize(imToken);
            os.write(EncryptUtil.desEncrypt(payload, CORE_KEY));

            return out.toByteArray();
        }
    }

    /**
     * deserialize
     *
     * @param data
     * @return
     * @throws IOException
     */
    private static ImToken deserialize(byte[] data) throws IOException {
        if (ArrayUtils.isEmpty(data)) {
            throw new ServiceRuntimeException("The input data is empty!");
        }

        try (ByteArrayInputStream in = new ByteArrayInputStream(data);
             DataInputStream is = new DataInputStream(in)
        ) {
            long appId = is.readLong();
            int coreKeyLength = is.readInt();
            is.readUTF();
            byte[] coreKey = new byte[coreKeyLength];
            int readCoreKeyBytes = is.read(coreKey);
            if (readCoreKeyBytes != coreKeyLength) {
                throw new ServiceRuntimeException("Failed to read the full coreKey!");
            }

            byte[] payload = new byte[is.available()];
            int readPayloadBytes = is.read(payload);
            if (readPayloadBytes != payload.length) {
                throw new ServiceRuntimeException("Failed to read the full payload!");
            }
            
            ImToken imToken = ProtostuffCodecUtil.deserialize(EncryptUtil.desDecrypt(payload, coreKey), ImToken.class);
            if (appId != imToken.getAppId()) {
                throw new ServiceRuntimeException("Invalid ImToken appId!");
            }

            return imToken;
        }
    }
}

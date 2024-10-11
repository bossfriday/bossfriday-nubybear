package cn.bossfriday.im.common.codec;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.Base58Util;
import cn.bossfriday.common.utils.ByteUtil;
import cn.bossfriday.common.utils.EncryptUtil;
import cn.bossfriday.common.utils.ProtostuffCodecUtil;
import cn.bossfriday.im.common.entity.ImToken;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Objects;

/**
 * ImTokenCodec
 *
 * @author chenx
 */
public class ImTokenCodec {

    private static final byte OBFUSCATE_BYTE = 0x64;
    private static final String OBFUSCATION_STRING = "BossFriday";

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
            long appSecretHash = imToken.getAppSecretHash();
            byte[] desKey = ByteUtil.long2Bytes(appSecretHash);

            os.writeLong(imToken.getAppId());
            os.writeUTF(OBFUSCATION_STRING);
            os.writeLong(appSecretHash);

            byte[] payload = ProtostuffCodecUtil.serialize(imToken);
            os.write(EncryptUtil.desEncrypt(payload, desKey));

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
            is.readUTF();
            long appSecretHash = is.readLong();
            byte[] desKey = ByteUtil.long2Bytes(appSecretHash);

            byte[] payload = new byte[is.available()];
            int readPayloadBytes = is.read(payload);
            if (readPayloadBytes != payload.length) {
                throw new ServiceRuntimeException("Failed to read the full payload!");
            }

            ImToken imToken = ProtostuffCodecUtil.deserialize(EncryptUtil.desDecrypt(payload, desKey), ImToken.class);
            if (appId != imToken.getAppId()) {
                throw new ServiceRuntimeException("Invalid ImToken appId!");
            }

            return imToken;
        }
    }
}

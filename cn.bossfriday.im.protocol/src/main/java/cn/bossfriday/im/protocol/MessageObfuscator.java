package cn.bossfriday.im.protocol;

import cn.bossfriday.im.protocol.enums.AccessKeyType;

import java.util.concurrent.ConcurrentHashMap;

import static cn.bossfriday.im.protocol.core.MqttException.OBFUSCATE_KEY_NOT_EXISTED_EXCEPTION;

/**
 * MessageObfuscator
 *
 * @author chenx
 */
public class MessageObfuscator {

    private static ConcurrentHashMap<Integer, byte[]> keyMap;

    private MessageObfuscator() {

    }

    static {
        keyMap = new ConcurrentHashMap<>();
        keyMap.put(AccessKeyType.KEY_104.getCode(), new byte[]{104, 80, 29, 15, 11, 31, 27, 127});
        keyMap.put(AccessKeyType.KEY_105.getCode(), new byte[]{105, 79, 18, 36, 14, 40, 21, 122});
        keyMap.put(AccessKeyType.KEY_106.getCode(), new byte[]{106, 79, 19, 35, 14, 41, 20, 121});
        keyMap.put(AccessKeyType.KEY_107.getCode(), new byte[]{107, 76, 22, 32, 17, 38, 23, 118});
        keyMap.put(AccessKeyType.KEY_108.getCode(), new byte[]{108, 77, 21, 33, 16, 39, 22, 119});
    }

    /**
     * obfuscateData
     *
     * @param data
     * @param start
     * @param keyType
     * @return
     */
    public static byte[] obfuscateData(byte[] data, int start, int keyType) {
        int dataLen = data.length;
        if (!keyMap.containsKey(keyType)) {
            throw OBFUSCATE_KEY_NOT_EXISTED_EXCEPTION;
        }

        byte[] key = keyMap.get(keyType);
        int keyLen = key.length;
        int b = 0;
        for (int i = start; i < dataLen; i += keyLen) {
            b = i;
            for (int j = 0; j < keyLen && b < dataLen; j++, b++) {
                data[b] = (byte) (data[b] ^ key[j]);
            }
        }

        return data;
    }
}

package cn.bossfriday.im.protocol;

import cn.bossfriday.common.utils.ByteUtil;

import java.util.concurrent.ConcurrentHashMap;

import static cn.bossfriday.im.protocol.core.MqttException.OBFUSCATE_KEY_NOT_EXISTED_EXCEPTION;

/**
 * MessageObfuscator
 *
 * @author chenx
 */
public class MessageObfuscator {

    public static final int ACCESS_KEY_TYPE_1 = 1;
    public static final int ACCESS_KEY_TYPE_2 = 2;
    public static final int ACCESS_KEY_TYPE_3 = 3;
    public static final int ACCESS_KEY_TYPE_4 = 4;

    private static ConcurrentHashMap<Integer, byte[]> keyMap;

    private MessageObfuscator() {

    }

    static {
        /**
         * 用随机的Long作为payload数据的混淆Key，这里hardcode几个；
         * 推荐的做法是：每个租户(AppKey)对应一个；
         */
        keyMap = new ConcurrentHashMap<>();
        keyMap.put(ACCESS_KEY_TYPE_1, ByteUtil.long2Bytes(2270231280823045204L));
        keyMap.put(ACCESS_KEY_TYPE_2, ByteUtil.long2Bytes(3515474498928459554L));
        keyMap.put(ACCESS_KEY_TYPE_3, ByteUtil.long2Bytes(6688311763110703555L));
        keyMap.put(ACCESS_KEY_TYPE_4, ByteUtil.long2Bytes(7591152070512697697L));
    }

    /**
     * obfuscateData
     *
     * @param data
     * @param start
     * @param accessKeyType
     * @return
     */
    public static byte[] obfuscateData(byte[] data, int start, int accessKeyType) {
        int dataLen = data.length;
        if (!keyMap.containsKey(accessKeyType)) {
            throw OBFUSCATE_KEY_NOT_EXISTED_EXCEPTION;
        }

        byte[] key = keyMap.get(accessKeyType);
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

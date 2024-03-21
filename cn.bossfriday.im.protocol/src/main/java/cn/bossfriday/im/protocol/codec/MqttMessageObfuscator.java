package cn.bossfriday.im.protocol.codec;

import cn.bossfriday.common.utils.ByteUtil;

/**
 * MqttMessageObfuscator
 *
 * @author chenx
 */
public class MqttMessageObfuscator {

    // 这里用一个随机的Long去hardCode一个key，推荐的做法是：每个租户(AppKey)对应一个混淆的Key
    private static final byte[] MQTT_KEY = ByteUtil.long2Bytes(6688311763110703555L);

    private MqttMessageObfuscator() {
        // do nothing
    }

    /**
     * obfuscateData
     *
     * @param data
     * @param start
     * @return
     */
    public static byte[] obfuscateData(byte[] data, int start) {
        int dataLen = data.length;
        int keyLen = MQTT_KEY.length;
        int b = 0;
        for (int i = start; i < dataLen; i += keyLen) {
            b = i;
            for (int j = 0; j < keyLen && b < dataLen; j++, b++) {
                data[b] = (byte) (data[b] ^ MQTT_KEY[j]);
            }
        }

        return data;
    }
}

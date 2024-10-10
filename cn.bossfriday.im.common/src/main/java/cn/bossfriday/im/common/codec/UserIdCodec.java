package cn.bossfriday.im.common.codec;

import cn.bossfriday.common.utils.Base58Util;
import cn.bossfriday.common.utils.UUIDUtil;

/**
 * UserIdCodec
 *
 * @author chenx
 */
public class UserIdCodec {

    private UserIdCodec() {
        // do nothing
    }

    /**
     * getUserId
     *
     * @return
     */
    public static String getUserId() {
        byte[] data = UUIDUtil.getUUIDBytes();
        return Base58Util.encode(data);
    }
}

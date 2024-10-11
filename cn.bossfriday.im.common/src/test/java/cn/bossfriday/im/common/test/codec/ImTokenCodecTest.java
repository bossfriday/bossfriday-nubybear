package cn.bossfriday.im.common.test.codec;

import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.im.common.codec.ImTokenCodec;
import cn.bossfriday.im.common.codec.UserIdCodec;
import cn.bossfriday.im.common.entity.ImToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ImTokenCodecTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class ImTokenCodecTest {

    @Before
    public void mockInit() {

    }

    @Test
    public void codecTest() {
        long appId = 100000L;
        String uid = UserIdCodec.getUserId();
        String deviceId = UUIDUtil.getRandomUUID().toString();
        long time = System.currentTimeMillis();
        String appSecret = "appSecret";

        ImToken imToken1 = new ImToken(appId, appSecret, uid, deviceId, time);
        String token = ImTokenCodec.encode(imToken1);
        System.out.println("token: " + token);

        ImToken imToken2 = ImTokenCodec.decode(token);
        System.out.println("ImToken: " + imToken2.toString());
        Assert.assertEquals(imToken1.getAppId(), imToken2.getAppId());
        Assert.assertEquals(imToken1.getUserId(), imToken2.getUserId());
        Assert.assertEquals(imToken1.getDeviceId(), imToken2.getDeviceId());
        Assert.assertEquals(imToken1.getTime(), imToken2.getTime());
    }
}

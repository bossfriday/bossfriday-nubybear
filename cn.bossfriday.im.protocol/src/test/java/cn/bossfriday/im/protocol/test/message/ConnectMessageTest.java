package cn.bossfriday.im.protocol.test.message;

import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.im.protocol.codec.MqttMessageInputStream;
import cn.bossfriday.im.protocol.enums.ClientType;
import cn.bossfriday.im.protocol.message.ConnectMessage;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * ConnectMessageTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectMessageTest {

    @Mock
    private ChannelHandlerContext mockCtx;

    @Before
    public void mockInit() {

    }

    @Test
    public void readWriteMessageTest() throws IOException {
        String deviceId = UUIDUtil.getShortString();
        String clientIp = "127.0.0.1";
        String appKey = "appKey";
        String token = "token";

        ConnectMessage connMsg = new ConnectMessage(deviceId, clientIp, true, 300);
        connMsg.setCredentials(appKey, token);
        connMsg.setWill("clientInfo", String.format("%s-%s-%s", ClientType.PC.getPlatform(), "1.0", "1.0"));

        byte[] msgData = connMsg.toBytes();
        ConnectMessage connMsg2 = (ConnectMessage) MqttMessageInputStream.readMessage(new ByteArrayInputStream(msgData), false);
        Assert.assertEquals(connMsg2.getToken(), token);
    }
}

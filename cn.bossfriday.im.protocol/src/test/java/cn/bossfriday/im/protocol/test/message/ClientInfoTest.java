package cn.bossfriday.im.protocol.test.message;

import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.im.protocol.client.ClientInfo;
import cn.bossfriday.im.protocol.enums.ClientType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ClientInfoTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientInfoTest {

    @Before
    public void mockInit() {

    }

    @Test
    public void willTest() {
        ClientType clientType = ClientType.PC;
        String deviceId = UUIDUtil.getRandomUUID().toString();
        String sdkVersion = "1.0";

        ClientInfo clientInfo1 = new ClientInfo(clientType, deviceId, sdkVersion);
        String will = ClientInfo.toWill(clientInfo1);
        System.out.println("will: " + will);

        ClientInfo clientInfo2 = ClientInfo.fromWill(will);
        System.out.println(ClientInfo.fromWill(will));
        Assert.assertEquals(clientInfo1.getDeviceId(), clientInfo2.getDeviceId());
    }
}

package cn.bossfriday.im.common.test.cluster;

import cn.bossfriday.im.common.message.user.GetTokenInput;
import cn.bossfriday.im.common.rpc.BaseActor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * BaseActorTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class BaseActorTest extends BaseActor<GetTokenInput> {

    @Override
    public void onMessageReceived(GetTokenInput msg) {
        // do nothing
    }

    @Test
    public void getRequestTypeTest() {
        BaseActorTest actor = new BaseActorTest();
        System.out.println(actor.getRequestType());
        Assert.assertEquals(actor.getRequestType(), GetTokenInput.class);
    }
}

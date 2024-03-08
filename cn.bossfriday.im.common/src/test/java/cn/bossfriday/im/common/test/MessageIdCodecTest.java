package cn.bossfriday.im.common.test;

import cn.bossfriday.im.common.entity.OpenMessageId;
import cn.bossfriday.im.common.enums.MessageType;
import cn.bossfriday.im.common.id.MessageIdCodec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static cn.bossfriday.im.common.id.MessageIdCodec.MESSAGE_ID_STRING_LENGTH;

/**
 * MessageIdCodecTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageIdCodecTest {

    @Before
    public void mockInit() {

    }

    @Test
    public void messageIdCodecTest() {
        // test messageIdEncode
        long msgTime = System.currentTimeMillis();
        int channelType = 1;
        String targetId = "user1";
        byte[] msgIdBytes1 = MessageIdCodec.messageIdSerialize(msgTime, channelType, targetId);
        String msgId1 = MessageIdCodec.messageIdEncode(msgIdBytes1);
        System.out.println("msgId: " + msgId1);
        Assert.assertEquals(MESSAGE_ID_STRING_LENGTH, msgId1.length());

        // test messageIdDecode
        byte[] msgIdBytes2 = MessageIdCodec.messageIdDecode(msgId1);
        String msgId2 = MessageIdCodec.messageIdEncode(msgIdBytes2);
        Assert.assertEquals(msgId1, msgId2);

        // test openMessageIdSerialize & openMessageIdEncode
        long time = msgTime + 123L;
        int msgType = MessageType.NB_TXT_MSG.getType();
        byte[] openMsgBytes = MessageIdCodec.openMessageIdSerialize(msgIdBytes2, time, msgType);
        String openMessageId = MessageIdCodec.openMessageIdEncode(openMsgBytes);
        System.out.println("openMessageId1: " + openMessageId);
        OpenMessageId result = MessageIdCodec.openMessageIdDecode(openMessageId);
        System.out.println("result: " + result);

        Assert.assertEquals(MESSAGE_ID_STRING_LENGTH, result.getMsgId().length());
        Assert.assertEquals(time, result.getTime());
        Assert.assertEquals(msgType, result.getMsgType());
    }
}

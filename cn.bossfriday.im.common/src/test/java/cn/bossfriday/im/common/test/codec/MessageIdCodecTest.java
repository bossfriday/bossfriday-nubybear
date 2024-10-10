package cn.bossfriday.im.common.test.codec;

import cn.bossfriday.im.common.codec.MessageIdCodec;
import cn.bossfriday.im.common.entity.OpenMessageId;
import cn.bossfriday.im.common.enums.MessageDirection;
import cn.bossfriday.im.common.enums.MessageType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.bossfriday.im.common.codec.MessageIdCodec.MESSAGE_ID_STRING_LENGTH;

/**
 * MessageIdCodecTest
 *
 * @author chenx
 */
@SuppressWarnings("all")
@RunWith(MockitoJUnitRunner.class)
public class MessageIdCodecTest {

    @Before
    public void mockInit() {

    }

    @Test
    public void msgIdWorkerTest() {
        // test messageIdEncode
        long msgTime = System.currentTimeMillis();
        System.out.println("msgTime:" + msgTime);
        int channelType = 1;
        String targetId = "user1";
        int msgDirection = MessageDirection.APPLICATION_TO_USER.getCode();

        byte[] msgIdBytes1 = MessageIdCodec.messageIdSerialize(msgTime, channelType, targetId);
        String msgId1 = MessageIdCodec.messageIdEncode(msgIdBytes1);
        System.out.println("msgId: " + msgId1);
        Assert.assertEquals(MESSAGE_ID_STRING_LENGTH, msgId1.length());

        // test messageIdDecode
        byte[] msgIdBytes2 = MessageIdCodec.messageIdDecode(msgId1);
        String msgId2 = MessageIdCodec.messageIdEncode(msgIdBytes2);
        Assert.assertEquals(msgId1, msgId2);

        // test openMessageIdSerialize & openMessageIdEncode
        int msgType = MessageType.NB_TXT_MSG.getType();
        byte[] openMsgBytes = MessageIdCodec.openMessageIdSerialize(msgIdBytes2, msgType, msgDirection);
        String openMessageId = MessageIdCodec.openMessageIdEncode(openMsgBytes);
        System.out.println("openMessageId1: " + openMessageId);
        OpenMessageId result = MessageIdCodec.openMessageIdDecode(openMessageId);
        System.out.println("result: " + result);

        Assert.assertEquals(MESSAGE_ID_STRING_LENGTH, result.getMsgId().length());
        Assert.assertEquals(msgTime, result.getTime());
        Assert.assertEquals(msgType, result.getMsgType());
        Assert.assertEquals(msgDirection, result.getMsgDirection());
    }

    public static void main(String[] args) {
        int msgType = MessageType.NB_TXT_MSG.getType();
        int msgDirection = MessageDirection.APPLICATION_TO_USER.getCode();

        // 多线程下并发测试
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                String msgId = MessageIdCodec.getMessageId(System.currentTimeMillis(), 1, "user1");
                String openMsgId = MessageIdCodec.getOpenMessageId(msgId, msgType, msgDirection);
                System.out.println("msgId: " + msgId + ", openMsgId:" + openMsgId);
            });
        }

        executorService.shutdown();
    }
}

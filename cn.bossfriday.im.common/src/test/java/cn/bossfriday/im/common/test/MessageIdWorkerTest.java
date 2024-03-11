package cn.bossfriday.im.common.test;

import cn.bossfriday.im.common.entity.OpenMessageId;
import cn.bossfriday.im.common.enums.MessageType;
import cn.bossfriday.im.common.id.MessageIdWorker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.bossfriday.im.common.id.MessageIdWorker.MESSAGE_ID_STRING_LENGTH;

/**
 * MessageIdCodecTest
 *
 * @author chenx
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageIdWorkerTest {

    @Before
    public void mockInit() {

    }

    @Test
    public void msgIdWorkerTest() {
        // test messageIdEncode
        long msgTime = System.currentTimeMillis();
        int channelType = 1;
        String targetId = "user1";
        byte[] msgIdBytes1 = MessageIdWorker.messageIdSerialize(msgTime, channelType, targetId);
        String msgId1 = MessageIdWorker.messageIdEncode(msgIdBytes1);
        System.out.println("msgId: " + msgId1);
        Assert.assertEquals(MESSAGE_ID_STRING_LENGTH, msgId1.length());

        // test messageIdDecode
        byte[] msgIdBytes2 = MessageIdWorker.messageIdDecode(msgId1);
        String msgId2 = MessageIdWorker.messageIdEncode(msgIdBytes2);
        Assert.assertEquals(msgId1, msgId2);

        // test openMessageIdSerialize & openMessageIdEncode
        long time = msgTime + 123L;
        int msgType = MessageType.NB_TXT_MSG.getType();
        byte[] openMsgBytes = MessageIdWorker.openMessageIdSerialize(msgIdBytes2, time, msgType);
        String openMessageId = MessageIdWorker.openMessageIdEncode(openMsgBytes);
        System.out.println("openMessageId1: " + openMessageId);
        OpenMessageId result = MessageIdWorker.openMessageIdDecode(openMessageId);
        System.out.println("result: " + result);

        Assert.assertEquals(MESSAGE_ID_STRING_LENGTH, result.getMsgId().length());
        Assert.assertEquals(time, result.getTime());
        Assert.assertEquals(msgType, result.getMsgType());
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    String msgId = MessageIdWorker.getMessageId(System.currentTimeMillis(), 1, "user1");
                    System.out.println(msgId);
                }
            });
        }
    }
}

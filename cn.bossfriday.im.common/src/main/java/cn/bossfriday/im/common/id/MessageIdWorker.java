package cn.bossfriday.im.common.id;

import cn.bossfriday.common.exception.ServiceRuntimeException;
import cn.bossfriday.common.utils.ByteUtil;
import cn.bossfriday.common.utils.MurmurHashUtil;
import cn.bossfriday.im.common.entity.OpenMessageId;
import cn.bossfriday.im.common.enums.MessageDirection;
import cn.bossfriday.im.common.enums.MessageType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Base64;

/**
 * MessageIdWorker
 * <p>
 * messageId：不对外公开的系统内部消息ID，例如：消息存储等适使用；
 * openMessageId：对外公开的消息ID，例如：调用IM开放平台HttpAPI发消息接口应答；
 *
 * @author chenx
 */
public class MessageIdWorker {

    public static final int MESSAGE_ID_BYTES_LENGTH = 10;
    public static final int MESSAGE_ID_STRING_LENGTH = 19;

    private MessageIdWorker() {

    }

    private static final int MAX_MESSAGE_SEQ = 0xFFF;
    private static final char BASE_32_ENCODE_CHARS_0 = '0';
    private static final char BASE_32_ENCODE_CHARS_9 = '9';
    private static final char BASE_32_ENCODE_CHARS_A = 'A';
    private static final char BASE_32_ENCODE_CHARS_Z = 'Z';
    private static final char[] BASE_32_ENCODE_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z'};

    private static int currentSeq = 0;

    /**
     * getOpenMessageId
     *
     * @param msgId
     * @param msgType
     * @return
     */
    public static String getOpenMessageId(String msgId, int msgType, int msgDirection) {
        byte[] msgIdBytes = MessageIdWorker.messageIdDecode(msgId);
        byte[] openMsgIdBytes = MessageIdWorker.openMessageIdSerialize(msgIdBytes, msgType, msgDirection);

        return MessageIdWorker.openMessageIdEncode(openMsgIdBytes);
    }

    /**
     * getMessageId
     *
     * @param time
     * @param channelType
     * @param targetId
     * @return
     */
    public static String getMessageId(long time, int channelType, String targetId) {
        byte[] msgIdBytes = MessageIdWorker.messageIdSerialize(time, channelType, targetId);

        return MessageIdWorker.messageIdEncode(msgIdBytes);
    }

    /**
     * messageIdSerialize
     * <p>
     * 消息时间戳（阉割版时间戳：最长可表示到2109年）：42位
     * 消息自旋ID：12位
     * 消息会话类型（例如：单聊，群聊，公众号消息等）：4位
     * 消息目标用户ID哈希值：22位
     *
     * @param time
     * @param channelType
     * @param targetId
     * @return
     */
    public static byte[] messageIdSerialize(long time, int channelType, String targetId) {
        int seq = getMessageSeq();
        time = time << 12;
        time = time | seq;

        time = time << 4;
        time = time | (channelType & 0xF);

        int targetIdInt = targetId.hashCode() & 0x3FFFFF;
        time = time << 6;
        time = time | (targetIdInt >> 16);
        int lowBits = (targetIdInt & 0xFFFF) << 16;


        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(outputStream);) {
            dos.writeLong(time);
            dos.writeInt(lowBits);

            byte[] data = outputStream.toByteArray();
            return ByteUtil.getBytesFromStart(data, MESSAGE_ID_BYTES_LENGTH);
        } catch (IOException e) {
            throw new ServiceRuntimeException("MessageIdCodec.serialize() failed!");
        }
    }

    /**
     * messageIdEncode
     * <p>
     * 将 10 字节的数据人为的分成 12 组 0 到 31 的无符号整数，
     * 然后根据每组的值映射到 BASE_32_ENCODE_CHARS 数组中的相应字符，
     * 同时每组用 "-" 切分，最终生成一个字符串；
     *
     * @param data
     * @return
     */
    public static String messageIdEncode(byte[] data) {
        checkMsgIdBytes(data);

        int b1;
        int b2;
        int b3;
        int b4;
        int b5;
        int b6;
        int b7;
        int b8;
        int b9;
        int b10;
        StringBuilder sb = new StringBuilder();

        b1 = data[0] & 0xff;
        b2 = data[1] & 0xff;
        b3 = data[2] & 0xff;
        b4 = data[3] & 0xff;
        b5 = data[4] & 0xff;
        b6 = data[5] & 0xff;
        b7 = data[6] & 0xff;
        b8 = data[7] & 0xff;
        b9 = data[8] & 0xff;
        b10 = data[9] & 0xff;

        sb.append(BASE_32_ENCODE_CHARS[b1 >>> 3]);
        sb.append(BASE_32_ENCODE_CHARS[((b1 & 0x7) << 2) | (b2 >>> 6)]);
        sb.append(BASE_32_ENCODE_CHARS[(b2 & 0x3e) >>> 1]);
        sb.append(BASE_32_ENCODE_CHARS[((b2 & 0x1) << 4) | (b3 >>> 4)]);
        sb.append("-");
        sb.append(BASE_32_ENCODE_CHARS[((b3 & 0xf) << 1) | (b4 >>> 7)]);
        sb.append(BASE_32_ENCODE_CHARS[(b4 & 0x7c) >>> 2]);
        sb.append(BASE_32_ENCODE_CHARS[((b4 & 0x3) << 3) | (b5 >>> 5)]);
        sb.append(BASE_32_ENCODE_CHARS[b5 & 0x1f]);
        sb.append("-");
        sb.append(BASE_32_ENCODE_CHARS[b6 >>> 3]);
        sb.append(BASE_32_ENCODE_CHARS[((b6 & 0x7) << 2) | (b7 >>> 6)]);
        sb.append(BASE_32_ENCODE_CHARS[(b7 & 0x3e) >>> 1]);
        sb.append(BASE_32_ENCODE_CHARS[((b7 & 0x1) << 4) | (b8 >>> 4)]);
        sb.append("-");
        sb.append(BASE_32_ENCODE_CHARS[((b8 & 0xf) << 1) | (b9 >>> 7)]);
        sb.append(BASE_32_ENCODE_CHARS[(b9 & 0x7c) >>> 2]);
        sb.append(BASE_32_ENCODE_CHARS[((b9 & 0x3) << 3) | (b10 >>> 5)]);
        sb.append(BASE_32_ENCODE_CHARS[b10 & 0x1f]);

        return sb.toString();
    }

    /**
     * messageIdDecode
     *
     * @param msgId
     * @return
     */
    public static byte[] messageIdDecode(String msgId) {
        if (StringUtils.isEmpty(msgId)) {
            throw new IllegalArgumentException("The input msgId is empty!");
        }

        if (msgId.length() != MESSAGE_ID_STRING_LENGTH) {
            throw new IllegalArgumentException("The input msgId is invalid!");
        }

        msgId = msgId.replace("-", "");
        int b1 = (findCharIndex(msgId.charAt(0)) << 3) | (findCharIndex(msgId.charAt(1)) >>> 2);
        int b2 = ((findCharIndex(msgId.charAt(1)) & 0x3) << 6) | (findCharIndex(msgId.charAt(2)) << 1) | (findCharIndex(msgId.charAt(3)) >>> 4);
        int b3 = ((findCharIndex(msgId.charAt(3)) & 0xF) << 4) | (findCharIndex(msgId.charAt(4)) >>> 1);
        int b4 = ((findCharIndex(msgId.charAt(4)) & 0x1) << 7) | (findCharIndex(msgId.charAt(5)) << 2) | (findCharIndex(msgId.charAt(6)) >>> 3);
        int b5 = ((findCharIndex(msgId.charAt(6)) & 0x7) << 5) | findCharIndex(msgId.charAt(7));

        int b6 = (findCharIndex(msgId.charAt(8)) << 3) | (findCharIndex(msgId.charAt(9)) >>> 2);
        int b7 = ((findCharIndex(msgId.charAt(9)) & 0x3) << 6) | (findCharIndex(msgId.charAt(10)) << 1) | (findCharIndex(msgId.charAt(11)) >>> 4);
        int b8 = ((findCharIndex(msgId.charAt(11)) & 0xF) << 4) | (findCharIndex(msgId.charAt(12)) >>> 1);
        int b9 = ((findCharIndex(msgId.charAt(12)) & 0x1) << 7) | (findCharIndex(msgId.charAt(13)) << 2) | (findCharIndex(msgId.charAt(14)) >>> 3);
        int b10 = ((findCharIndex(msgId.charAt(14)) & 0x7) << 5) | findCharIndex(msgId.charAt(15));

        byte[] data = new byte[MESSAGE_ID_BYTES_LENGTH];
        data[0] = (byte) (b1 & 0xff);
        data[1] = (byte) (b2 & 0xff);
        data[2] = (byte) (b3 & 0xff);
        data[3] = (byte) (b4 & 0xff);
        data[4] = (byte) (b5 & 0xff);
        data[5] = (byte) (b6 & 0xff);
        data[6] = (byte) (b7 & 0xff);
        data[7] = (byte) (b8 & 0xff);
        data[8] = (byte) (b9 & 0xff);
        data[9] = (byte) (b10 & 0xff);

        return data;
    }

    /**
     * getMessageTime
     *
     * @param msgId
     * @return
     */
    public static long getMessageTime(String msgId) {
        byte[] msgIdBytes = messageIdDecode(msgId);

        return getMessageTime(msgIdBytes);
    }

    /**
     * openMessageIdSerialize
     * <p>
     * 2字节：short hash值
     * 10字节：消息ID
     * 1字节：消息类型
     * 1字节：消息方向
     */
    public static byte[] openMessageIdSerialize(byte[] msgIdBytes, int msgType, int msgDirection) {
        checkMsgIdBytes(msgIdBytes);
        MessageType messageType = MessageType.getByType(msgType);
        if (ObjectUtils.isEmpty(messageType)) {
            throw new ServiceRuntimeException("unsupported msgType(" + msgType + ")!");
        }

        MessageDirection messageDirection = MessageDirection.getByCode(msgDirection);
        if (ObjectUtils.isEmpty(messageDirection)) {
            throw new ServiceRuntimeException("unsupported messageDirection(" + messageDirection + ")!");
        }

        byte[] msgTypeBytes = new byte[]{messageType.getType()};
        byte[] msgDirectionBytes = new byte[]{(byte) msgDirection};
        byte[] data = ByteUtil.mergeBytes(msgIdBytes, msgTypeBytes, msgDirectionBytes);
        short dataHash = (short) MurmurHashUtil.hash32(data);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {
            dos.writeShort(dataHash);
            dos.write(msgIdBytes);
            dos.write(msgTypeBytes);
            dos.write(msgDirectionBytes);

            return out.toByteArray();
        } catch (IOException ex) {
            throw new ServiceRuntimeException("openMessageIdSerialize() error! " + ex.getMessage());
        }
    }

    /**
     * openMessageIdEncode
     *
     * @param data
     * @return
     */
    public static String openMessageIdEncode(byte[] data) {
        ByteUtil.hashObfuscate(data, Short.BYTES);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * openMessageIdDecode
     *
     * @param openMsgId
     * @return
     */
    public static OpenMessageId openMessageIdDecode(String openMsgId) {
        if (StringUtils.isEmpty(openMsgId)) {
            throw new IllegalArgumentException("openMessageIdDecode error!(The input openMsgId is empty)");
        }

        byte[] bytes = Base64.getUrlDecoder().decode(openMsgId);
        ByteUtil.hashObfuscate(bytes, Short.BYTES);

        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(in)
        ) {
            byte[] msgIdBytes = new byte[MESSAGE_ID_BYTES_LENGTH];
            dis.readShort();
            if (dis.read(msgIdBytes) != MESSAGE_ID_BYTES_LENGTH) {
                throw new ServiceRuntimeException("openMessageIdDecode error!(read msgIdBytes error) ");
            }

            byte msgTypeByte = dis.readByte();
            byte msgDirectionByte = dis.readByte();

            MessageType msgType = MessageType.getByType(msgTypeByte);
            if (ObjectUtils.isEmpty(msgType)) {
                throw new ServiceRuntimeException("unsupported msgType(" + msgTypeByte + ")!");
            }

            MessageDirection messageDirection = MessageDirection.getByCode(msgDirectionByte);
            if (ObjectUtils.isEmpty(messageDirection)) {
                throw new ServiceRuntimeException("unsupported messageDirection(" + msgDirectionByte + ")!");
            }

            String msgId = messageIdEncode(msgIdBytes);
            long time = getMessageTime(msgIdBytes);

            return new OpenMessageId(msgId, msgType.getType(), time, messageDirection.getCode());
        } catch (Exception ex) {
            throw new ServiceRuntimeException("openMessageIdDecode error!(" + ex.getMessage() + ")");
        }
    }

    /**
     * getMessageTime
     *
     * @param msgIdBytes
     * @return
     */
    private static long getMessageTime(byte[] msgIdBytes) {
        checkMsgIdBytes(msgIdBytes);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(msgIdBytes);
             DataInputStream dataInputStream = new DataInputStream(inputStream)) {

            return dataInputStream.readLong() >>> 22;

        } catch (IOException e) {
            throw new IllegalArgumentException("The input msgIdBytes is invalid! ");
        }
    }


    /**
     * findCharIndex
     *
     * @param c
     * @return
     */
    private static int findCharIndex(char c) {
        if (c >= BASE_32_ENCODE_CHARS_0 && c <= BASE_32_ENCODE_CHARS_9) {
            return c - 48;
        }

        if (c >= BASE_32_ENCODE_CHARS_A && c <= BASE_32_ENCODE_CHARS_Z) {
            return c - 55;
        }

        throw new IllegalArgumentException("Invalid character in messageId: " + c);
    }

    /**
     * checkMsgIdBytes
     *
     * @param msgIdBytes
     */
    private static void checkMsgIdBytes(byte[] msgIdBytes) {
        if (ArrayUtils.isEmpty(msgIdBytes)) {
            throw new IllegalArgumentException("The input msgIdBytes is empty!");
        }

        if (msgIdBytes.length != MESSAGE_ID_BYTES_LENGTH) {
            throw new IllegalArgumentException("The input msgIdBytes.length must be " + MESSAGE_ID_BYTES_LENGTH + "!");
        }
    }

    /**
     * getMessageSeq
     *
     * @return
     */
    private static synchronized int getMessageSeq() {
        int ret = currentSeq++;

        if (ret > MAX_MESSAGE_SEQ) {
            currentSeq = 0;
            ret = currentSeq++;
        }

        return ret;
    }
}

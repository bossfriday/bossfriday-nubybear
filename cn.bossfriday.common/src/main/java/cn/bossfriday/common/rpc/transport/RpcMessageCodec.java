package cn.bossfriday.common.rpc.transport;


import cn.bossfriday.common.utils.UUIDUtil;
import cn.bossfriday.common.utils.ByteUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RpcMessageCodec {
    private static final String CHARSET = "UTF-8";
    private static final int MAX_VALUE_UNSIGNED_INT8 = 255;
    private static final int MAX_VALUE_UNSIGNED_INT16 = 65535;
    private static final int MAX_VALUE_UNSIGNED_INT24 = 16777215;

    /**
     * serialize
     */
    public static byte[] serialize(RpcMessage msg) throws Exception {
        ByteArrayOutputStream bos = null;
        DataOutputStream out = null;
        try {
            bos = new ByteArrayOutputStream();
            out = new DataOutputStream(bos);

            out.write(msg.getSession());
            writeMethod(out, msg.getTargetMethod());
            writeMethod(out, msg.getSourceMethod());
            out.write(ByteUtil.ipToBytes(msg.getSourceHost()));
            out.write(ByteUtil.ipToBytes(msg.getTargetHost()));
            out.writeInt(msg.getSourcePort());
            out.writeInt(msg.getTargetPort());
            out.writeLong(msg.getTimestamp());
            out.writeByte(msg.getVersion());
            writePayloadData(out, msg.getPayloadData());

            return bos.toByteArray();
        } finally {
            try {
                if (out != null)
                    out.close();

                if (bos != null)
                    bos.close();
            } catch (Exception e) {
                log.warn("Message.serialize() close error!", e);
            }
        }
    }


    /**
     * deserialize
     */
    public static RpcMessage deserialize(byte[] data) throws Exception {
        ByteArrayInputStream bis = null;
        DataInputStream in = null;
        try {
            bis = new ByteArrayInputStream(data);
            in = new DataInputStream(bis);
            RpcMessage msg = new RpcMessage();

            byte[] sessionBytes = new byte[16];
            in.readFully(sessionBytes);
            msg.setSession(sessionBytes);

            msg.setTargetMethod(readMethod(in));
            msg.setSourceMethod(readMethod(in));

            byte[] sourceHostBytes = new byte[4];
            in.readFully(sourceHostBytes);
            msg.setSourceHost(ByteUtil.bytesToIp(sourceHostBytes));

            byte[] targetHostBytes = new byte[4];
            in.readFully(targetHostBytes);
            msg.setTargetHost(ByteUtil.bytesToIp(targetHostBytes));

            msg.setSourcePort(in.readInt());
            msg.setTargetPort(in.readInt());
            msg.setTimestamp(in.readLong());
            msg.setVersion(in.readByte());
            msg.setPayloadData(readPayloadData(in));

            return msg;
        } finally {
            try {
                if (in != null)
                    in.close();

                if (bis != null)
                    bis.close();
            } catch (Exception e) {
                log.warn("Message.deserialize() close error!", e);
            }
        }
    }

    /**
     * bytesToUnsignedInt16
     */
    public static int bytesToUnsignedInt16(byte[] bytes) throws Exception {
        if (bytes.length != 2)
            throw new Exception("bytes.length must be 2.");

        return bytes[0] << 8 & 0xFF00 | bytes[1] & 0xFF;
    }

    /**
     * unsignedInt16ToBytes
     */
    public static byte[] unsignedInt16ToBytes(int value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) ((value & 0xFF00) >> 8);
        bytes[1] = (byte) (value & 0xFF);

        return bytes;
    }

    /**
     * bytesToUnsignedInt24
     */
    public static int bytesToUnsignedInt24(byte[] bytes) throws Exception {
        if (bytes.length != 3)
            throw new Exception("bytes.length must be 3.");

        return (bytes[2] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[0] & 0x0F) << 16);
    }

    /**
     * unsignedInt24ToBytes
     */
    public static byte[] unsignedInt24ToBytes(int value) {
        byte[] bytes = new byte[3];
        bytes[0] = (byte) ((value >> 16) & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[2] = (byte) (value & 0xFF);

        return bytes;
    }


    /**
     * writeMethod
     *
     * @param out
     * @param str
     * @throws Exception
     */
    private static void writeMethod(DataOutputStream out, String str) throws Exception {
        if (StringUtils.isEmpty(str)) {
            out.writeByte((byte) 0);
            return;
        }

        byte[] bytes = str.getBytes(CHARSET);
        int length = bytes.length;
        if (length > MAX_VALUE_UNSIGNED_INT8)
            throw new Exception("encode string too long, length:" + length);

        out.writeByte((byte) length);
        out.write(bytes);
    }

    /**
     * readMethod
     *
     * @param in
     * @return
     * @throws Exception
     */
    private static String readMethod(DataInputStream in) throws Exception {
        int length = Byte.toUnsignedInt(in.readByte());
        if (length <= 0) {
            return null;
        }

        byte[] bytes = new byte[length];
        in.readFully(bytes);

        return new String(bytes, CHARSET);
    }

    /**
     * writePayloadData
     *
     * @param out
     * @param data
     * @throws Exception
     */
    private static void writePayloadData(DataOutputStream out, byte[] data) throws Exception {
        if (data == null || data.length == 0) {
            out.writeByte((byte) 0);
            return;
        }

        int length = data.length;
        if (length > MAX_VALUE_UNSIGNED_INT24)
            throw new Exception("encode data too long, length:" + length);

        out.write(unsignedInt24ToBytes(length));
        out.write(data);
    }

    /**
     * readPayloadData
     *
     * @param in
     * @return
     * @throws Exception
     */
    private static byte[] readPayloadData(DataInputStream in) throws Exception {
        byte[] lenBytes = new byte[3];
        in.readFully(lenBytes);
        int length = bytesToUnsignedInt24(lenBytes);
        if (length <= 0) {
            return null;
        }


        byte[] bytes = new byte[length];
        in.readFully(bytes);

        return bytes;
    }

    public static void main(String[] args) throws Exception {
        RpcMessage msg = new RpcMessage();
        msg.setSession(UUIDUtil.getUUIDBytes());
        msg.setTargetMethod("targetMethod");
        msg.setSourceMethod("sourceMethod");
        msg.setSourceHost("192.168.0.1");
        msg.setTargetHost("192.168.0.2");
        msg.setSourcePort(8081);
        msg.setTargetPort(8082);
        msg.setTimestamp(System.currentTimeMillis());
        msg.setPayloadData("hello".getBytes(StandardCharsets.UTF_8));

        System.out.println(msg.toString());
        System.out.println(RpcMessageCodec.deserialize(RpcMessageCodec.serialize(msg)).toString());
    }
}

package cn.bossfriday.common.rpc.transport;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.ByteUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static cn.bossfriday.common.utils.ByteUtil.*;

/**
 * RpcMessageCodec
 *
 * @author chenx
 */
@Slf4j
public class RpcMessageCodec {

    private RpcMessageCodec() {

    }

    /**
     * serialize
     *
     * @param msg
     * @return
     * @throws IOException
     */
    public static byte[] serialize(RpcMessage msg) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bos)) {
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
        }
    }

    /**
     * deserialize
     *
     * @param data
     * @return
     * @throws IOException
     */
    public static RpcMessage deserialize(byte[] data) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             DataInputStream in = new DataInputStream(bis)) {
            RpcMessage msg = new RpcMessage();

            byte[] sessionBytes = new byte[16];
            in.readFully(sessionBytes);
            msg.setSession(sessionBytes);

            msg.setTargetMethod(readMethod(in));
            msg.setSourceMethod(readMethod(in));

            byte[] sourceHostBytes = new byte[INT_BYTES_LENGTH];
            in.readFully(sourceHostBytes);
            msg.setSourceHost(ByteUtil.bytesToIp(sourceHostBytes));

            byte[] targetHostBytes = new byte[INT_BYTES_LENGTH];
            in.readFully(targetHostBytes);
            msg.setTargetHost(ByteUtil.bytesToIp(targetHostBytes));

            msg.setSourcePort(in.readInt());
            msg.setTargetPort(in.readInt());
            msg.setTimestamp(in.readLong());
            msg.setVersion(in.readByte());
            msg.setPayloadData(readPayloadData(in));

            return msg;
        }
    }

    /**
     * writeMethod
     *
     * @param out
     * @param str
     * @throws BizException
     * @throws IOException
     */
    private static void writeMethod(DataOutputStream out, String str) throws IOException {
        if (StringUtils.isEmpty(str)) {
            out.writeByte((byte) 0);
            return;
        }

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        int length = bytes.length;
        if (length > MAX_VALUE_UNSIGNED_INT8) {
            throw new BizException("encode string too long, length:" + length);
        }

        out.writeByte((byte) length);
        out.write(bytes);
    }

    /**
     * readMethod
     *
     * @param in
     * @return
     * @throws IOException
     */
    private static String readMethod(DataInputStream in) throws IOException {
        int length = Byte.toUnsignedInt(in.readByte());
        if (length <= 0) {
            return null;
        }

        byte[] bytes = new byte[length];
        in.readFully(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * writePayloadData
     *
     * @param out
     * @param data
     * @throws IOException
     */
    private static void writePayloadData(DataOutputStream out, byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            out.writeByte((byte) 0);
            return;
        }

        int length = data.length;
        if (length > MAX_VALUE_UNSIGNED_INT24) {
            throw new BizException("encode data too long, length:" + length);
        }

        out.write(ByteUtil.unsignedInt24ToBytes(length));
        out.write(data);
    }

    /**
     * readPayloadData
     *
     * @param in
     * @return
     * @throws IOException
     */
    private static byte[] readPayloadData(DataInputStream in) throws IOException {
        byte[] lenBytes = new byte[INT_24_BYTES_LENGTH];
        in.readFully(lenBytes);
        int length = ByteUtil.bytesToUnsignedInt24(lenBytes);
        if (length <= 0) {
            return new byte[0];
        }

        byte[] bytes = new byte[length];
        in.readFully(bytes);

        return bytes;
    }
}

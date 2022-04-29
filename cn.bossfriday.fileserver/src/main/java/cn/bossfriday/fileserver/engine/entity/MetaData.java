package cn.bossfriday.fileserver.engine.entity;

import cn.bossfriday.common.utils.GsonUtil;
import cn.bossfriday.fileserver.engine.core.ICodec;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

@Slf4j
@Data
@Builder
public class MetaData implements ICodec<MetaData> {

    private int storeEngineVersion;    // 存储引擎版本
    private int fileStatus;            // 文件状态标识
    private long timestamp;             // 上传时间戳
    private String fileName;            // 文件名
    private long fileTotalSize;         // 文件大小

    public MetaData() {

    }

    public MetaData(int storeEngineVersion, int fileStatus, long timestamp, String fileName, long fileTotalSize) {
        this.storeEngineVersion = (byte) storeEngineVersion;
        this.fileStatus = (byte) fileStatus;
        this.timestamp = timestamp;
        this.fileName = fileName;
        this.fileTotalSize = fileTotalSize;
    }

    @Override
    public byte[] serialize() throws Exception {
        ByteArrayOutputStream out = null;
        DataOutputStream dos = null;

        try {
            out = new ByteArrayOutputStream();
            dos = new DataOutputStream(out);

            dos.writeByte((byte) storeEngineVersion);
            dos.writeByte((byte) fileStatus);
            dos.writeLong(timestamp);
            dos.writeUTF(fileName);
            dos.writeLong(fileTotalSize);

            return out.toByteArray();
        } finally {
            try {
                if (dos != null)
                    dos.close();

                if (out != null)
                    out.close();
            } catch (Exception ex) {
                log.warn("serialize release error!", ex);
            }
        }
    }

    @Override
    public MetaData deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream in = null;
        DataInputStream dis = null;
        try {
            in = new ByteArrayInputStream(bytes);
            dis = new DataInputStream(in);

            int storeEngineVersion = Byte.toUnsignedInt(dis.readByte());
            int fileStatus = Byte.toUnsignedInt(dis.readByte());
            long timestamp = dis.readLong();
            String fileName = dis.readUTF();
            long fileTotalSize = dis.readLong();

            return MetaData.builder()
                    .storeEngineVersion(storeEngineVersion)
                    .fileStatus(fileStatus)
                    .timestamp(timestamp)
                    .fileName(fileName)
                    .fileTotalSize(fileTotalSize)
                    .build();
        } finally {
            try {
                if (dis != null)
                    dis.close();

                if (in != null)
                    in.close();
            } catch (Exception ex) {
                log.warn("deserialize release error!", ex);
            }
        }
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }

    public static void main(String[] args) throws Exception {
        String fileName = "1.jpg";
        long fileTotalSize = 100L;
        MetaData data1 = new MetaData(1, 0, System.currentTimeMillis(), fileName, fileTotalSize);
        System.out.println(data1.toString());
        byte[] data = data1.serialize();
        MetaData data2 = new MetaData().deserialize(data);
        System.out.println(data2.toString());
    }
}

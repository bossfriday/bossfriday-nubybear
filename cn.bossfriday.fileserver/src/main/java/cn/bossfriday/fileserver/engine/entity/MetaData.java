package cn.bossfriday.fileserver.engine.entity;

import cn.bossfriday.fileserver.engine.core.ICodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

@Slf4j
@Builder
public class MetaData implements ICodec<MetaData> {
    @Getter
    @Setter
    private byte storeEngineVersion;    // 存储引擎版本

    @Getter
    @Setter
    private byte fileStatus;            // 文件状态标识

    @Getter
    @Setter
    private long timestamp;             // 上传时间戳

    @Getter
    @Setter
    private String fileName;            // 文件名

    @Getter
    @Setter
    private long fileTotalSize;         // 文件大小

    public MetaData() {

    }

    public MetaData(byte storeEngineVersion, byte fileStatus, long timestamp, String fileName, long fileTotalSize) {
        this.storeEngineVersion = storeEngineVersion;
        this.fileStatus = fileStatus;
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

            dos.writeByte(storeEngineVersion);
            dos.writeByte(fileStatus);
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

            byte storeEngineVersion = dis.readByte();
            byte fileStatus = dis.readByte();
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
        return "MetaData{" +
                "storeEngineVersion=" + Byte.toUnsignedInt(storeEngineVersion) +
                ", fileStatus=" + Byte.toUnsignedInt(fileStatus) +
                ", timestamp=" + timestamp +
                ", fileName='" + fileName + '\'' +
                ", fileTotalSize=" + fileTotalSize +
                '}';
    }

    public static void main(String[] args) throws Exception {
        MetaData data1 = new MetaData((byte) 1, (byte) 0, System.currentTimeMillis(), "1.jpg", 100L);
        System.out.println(data1.toString());
        byte[] data = data1.serialize();
        MetaData data2 = new MetaData().deserialize(data);
        System.out.println(data2.toString());
    }
}

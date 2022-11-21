package cn.bossfriday.fileserver.engine.model;

import cn.bossfriday.fileserver.engine.core.ICodec;
import lombok.*;

import java.io.*;

/**
 * MetaData
 *
 * @author chenx
 */
@ToString
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetaData implements ICodec<MetaData> {

    public static final int STORE_ENGINE_VERSION_LENGTH = 1;
    public static final int FILE_STATUS_LENGTH = 1;
    public static final int TIMESTAMP_LENGTH = 8;
    public static final int UTF8_FIRST_SIGNIFICANT_LENGTH = 2;
    public static final int FILE_TOTAL_SIZE_LENGTH = 8;

    /**
     * 存储引擎版本（1字节）
     */
    private int storeEngineVersion;

    /**
     * 文件状态标识（1字节）：
     * 后续如果要扩展更多标志位（1字节最多表达8个标志位），
     * 可以新引入一个storageEngineVersion去实现（即新版本下fileStatus改为2字节）
     */
    private int fileStatus;

    /**
     * 上传时间戳（8字节）
     */
    private long timestamp;

    /**
     * 文件名（utf8字符串）
     */
    private String fileName;

    /**
     * 文件大小（8字节）
     */
    private long fileTotalSize;

    @Override
    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {
            dos.writeByte((byte) this.storeEngineVersion);
            dos.writeByte((byte) this.fileStatus);
            dos.writeLong(this.timestamp);
            dos.writeUTF(this.fileName);
            dos.writeLong(this.fileTotalSize);

            return out.toByteArray();
        }
    }

    @Override
    public MetaData deserialize(byte[] bytes) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(in)) {
            int version = Byte.toUnsignedInt(dis.readByte());
            int status = Byte.toUnsignedInt(dis.readByte());
            long ts = dis.readLong();
            String fName = dis.readUTF();
            long fSize = dis.readLong();

            return MetaData.builder()
                    .storeEngineVersion(version)
                    .fileStatus(status)
                    .timestamp(ts)
                    .fileName(fName)
                    .fileTotalSize(fSize)
                    .build();
        }
    }
}

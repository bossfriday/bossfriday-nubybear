package cn.bossfriday.fileserver.engine.model;

import cn.bossfriday.fileserver.engine.core.ICodec;
import lombok.*;

import java.io.*;

/**
 * RecoverableTmpFile
 *
 * @author chenx
 */
@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecoverableTmpFile implements ICodec<RecoverableTmpFile>, Comparable<RecoverableTmpFile> {

    /**
     * fileTransactionId
     */
    private String fileTransactionId;

    /**
     * storeEngineVersion
     */
    private int storeEngineVersion;

    /**
     * storageNamespace
     */
    private String storageNamespace;

    /**
     * time
     */
    private int time;

    /**
     * offset
     */
    private long offset;

    /**
     * timestamp
     */
    private long timestamp;

    /**
     * fileName
     */
    private String fileName;

    /**
     * fileTotalSize
     */
    private long fileTotalSize;

    /**
     * filePath
     */
    private String filePath;

    @Override
    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {
            dos.writeUTF(this.fileTransactionId);
            dos.writeInt(this.storeEngineVersion);
            dos.writeUTF(this.storageNamespace);
            dos.writeInt(this.time);
            dos.writeLong(this.offset);
            dos.writeLong(this.timestamp);
            dos.writeUTF(this.fileName);
            dos.writeLong(this.fileTotalSize);
            dos.writeUTF(this.filePath);

            return out.toByteArray();
        }
    }

    @Override
    public RecoverableTmpFile deserialize(byte[] bytes) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(in)) {
            String fileTransactionIdValue = dis.readUTF();
            int storeEngineVersionValue = dis.readInt();
            String storageNamespaceValue = dis.readUTF();
            int timeValue = dis.readInt();
            long offsetValue = dis.readLong();
            long timestampValue = dis.readLong();
            String fileNameValue = dis.readUTF();
            long fileTotalSizeValue = dis.readLong();
            String filePathValue = dis.readUTF();

            return RecoverableTmpFile.builder()
                    .fileTransactionId(fileTransactionIdValue)
                    .storeEngineVersion(storeEngineVersionValue)
                    .storageNamespace(storageNamespaceValue)
                    .time(timeValue)
                    .offset(offsetValue)
                    .timestamp(timestampValue)
                    .fileName(fileNameValue)
                    .fileTotalSize(fileTotalSizeValue)
                    .filePath(filePathValue)
                    .build();
        }
    }

    @Override
    public int compareTo(RecoverableTmpFile other) {
        int timeComparison = Integer.compare(this.getTime(), other.getTime());
        if (timeComparison != 0) {
            return timeComparison;
        }

        return Long.compare(this.getOffset(), other.getOffset());
    }
}


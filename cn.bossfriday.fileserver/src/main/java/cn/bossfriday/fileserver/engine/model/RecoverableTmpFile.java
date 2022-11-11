package cn.bossfriday.fileserver.engine.model;

import lombok.*;

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
public class RecoverableTmpFile {

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
}

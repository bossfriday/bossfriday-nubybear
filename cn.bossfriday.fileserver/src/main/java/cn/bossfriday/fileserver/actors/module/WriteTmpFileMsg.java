package cn.bossfriday.fileserver.actors.module;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WriteTmpFileMsg
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteTmpFileMsg {

    /**
     * storageEngineVersion
     */
    private int storageEngineVersion;

    /**
     * fileTransactionId
     */
    private String fileTransactionId;

    /**
     * storageNamespace
     */
    private String storageNamespace;

    /**
     * isKeepAlive
     */
    private boolean isKeepAlive;

    /**
     * fileName
     */
    private String fileName;

    /**
     * filePartitionSize
     */
    private long filePartitionSize;

    /**
     * fileTotalSize
     */
    private long fileTotalSize;

    /**
     * offset
     */
    private long offset;

    /**
     * data
     */
    private byte[] data;
}

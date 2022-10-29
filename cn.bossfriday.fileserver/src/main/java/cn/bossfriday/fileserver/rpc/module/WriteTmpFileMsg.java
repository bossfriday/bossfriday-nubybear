package cn.bossfriday.fileserver.rpc.module;

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
     * namespace
     */
    private String namespace;

    /**
     * isKeepAlive
     */
    private boolean isKeepAlive;

    /**
     * fileName
     */
    private String fileName;

    /**
     * fileSize
     */
    private long fileSize;

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

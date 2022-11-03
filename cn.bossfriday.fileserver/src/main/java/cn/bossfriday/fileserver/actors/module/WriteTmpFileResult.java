package cn.bossfriday.fileserver.actors.module;

import cn.bossfriday.fileserver.common.enums.OperationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WriteTmpFileResult
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteTmpFileResult {

    /**
     * fileTransactionId
     */
    private String fileTransactionId;

    /**
     * result
     */
    private OperationResult result;

    /**
     * storageEngineVersion
     */
    private int storageEngineVersion;

    /**
     * clusterNodeName
     */
    private String clusterNodeName;

    /**
     * storageNamespace
     */
    private String storageNamespace;

    /**
     * isKeepAlive
     */
    private boolean isKeepAlive;

    /**
     * timestamp
     */
    private long timestamp;

    /**
     * fileTotalSize
     */
    private long fileTotalSize;

    /**
     * fileName
     */
    private String fileName;

    /**
     * fileExtName
     */
    private String fileExtName;

    /**
     * filePath
     */
    private String filePath;

    public WriteTmpFileResult(String fileTransactionId, OperationResult result) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
    }
}

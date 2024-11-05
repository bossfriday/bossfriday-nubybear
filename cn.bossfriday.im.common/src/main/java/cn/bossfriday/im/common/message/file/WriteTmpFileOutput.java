package cn.bossfriday.im.common.message.file;

import cn.bossfriday.common.http.model.Range;
import cn.bossfriday.im.common.enums.file.OperationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WriteTmpFileOutput
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteTmpFileOutput {

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

    /**
     * range
     */
    private Range range;

    public WriteTmpFileOutput(String fileTransactionId, OperationResult result) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
    }

    /**
     * isFullDone
     *
     * @return
     */
    public boolean isFullDone() {
        return this.range == null;
    }
}

package cn.bossfriday.fileserver.rpc.module;

import cn.bossfriday.fileserver.common.enums.OperationResult;
import lombok.Data;

@Data
public class WriteTmpFileResult {
    private String fileTransactionId;
    private OperationResult result;
    private int storageEngineVersion;
    private String clusterNodeName;
    private String namespace;
    private boolean isKeepAlive;
    private long timestamp;
    private long fileTotalSize;
    private String fileName;
    private String fileExtName;

    public WriteTmpFileResult() {

    }

    public WriteTmpFileResult(String fileTransactionId, OperationResult result) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
    }
}

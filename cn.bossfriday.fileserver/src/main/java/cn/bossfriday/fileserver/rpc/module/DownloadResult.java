package cn.bossfriday.fileserver.rpc.module;


import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.entity.ChunkedFileData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadResult {
    private String fileTransactionId;
    private OperationResult result;
    private ChunkedFileData chunkedFileData;

    public DownloadResult() {

    }

    public DownloadResult(String fileTransactionId, OperationResult result) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
    }

    public DownloadResult(String fileTransactionId, OperationResult result, ChunkedFileData chunkedFileData) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
        this.chunkedFileData = chunkedFileData;
    }
}

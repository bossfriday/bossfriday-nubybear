package cn.bossfriday.fileserver.rpc.module;


import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.entity.ChunkedMetaData;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadResult {
    private String fileTransactionId;
    private OperationResult result;
    private MetaDataIndex metaDataIndex;
    private long chunkIndex;
    private long chunkCount;
    private ChunkedMetaData chunkedMetaData;

    public DownloadResult() {

    }

    public DownloadResult(String fileTransactionId, OperationResult result) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
    }

    public DownloadResult(String fileTransactionId, OperationResult result, MetaDataIndex metaDataIndex, long chunkIndex, long chunkCount, ChunkedMetaData chunkedMetaData) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
        this.metaDataIndex = metaDataIndex;
        this.chunkIndex = chunkIndex;
        this.chunkCount = chunkCount;
        this.chunkedMetaData = chunkedMetaData;
    }
}

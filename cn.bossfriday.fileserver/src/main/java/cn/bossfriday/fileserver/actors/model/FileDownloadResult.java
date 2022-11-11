package cn.bossfriday.fileserver.actors.model;

import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.model.ChunkedMetaData;
import cn.bossfriday.fileserver.engine.model.MetaData;
import cn.bossfriday.fileserver.engine.model.MetaDataIndex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileDownloadResult
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadResult {

    /**
     * fileTransactionId
     */
    private String fileTransactionId;

    /**
     * result
     */
    private OperationResult result;

    /**
     * metaDataIndex
     */
    private MetaDataIndex metaDataIndex;

    /**
     * metaData
     */
    private MetaData metaData;

    /**
     * chunkIndex
     */
    private long chunkIndex;

    /**
     * chunkCount
     */
    private long chunkCount;

    /**
     * chunkedMetaData
     */
    private ChunkedMetaData chunkedMetaData;

    public FileDownloadResult(String fileTransactionId, OperationResult result) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
    }
}

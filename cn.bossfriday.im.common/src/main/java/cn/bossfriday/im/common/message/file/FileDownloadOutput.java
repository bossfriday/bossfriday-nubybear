package cn.bossfriday.im.common.message.file;

import cn.bossfriday.im.common.entity.file.ChunkedMetaData;
import cn.bossfriday.im.common.entity.file.MetaData;
import cn.bossfriday.im.common.entity.file.MetaDataIndex;
import cn.bossfriday.im.common.enums.file.OperationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileDownloadOutput
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadOutput {

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

    public FileDownloadOutput(String fileTransactionId, OperationResult result) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
    }
}

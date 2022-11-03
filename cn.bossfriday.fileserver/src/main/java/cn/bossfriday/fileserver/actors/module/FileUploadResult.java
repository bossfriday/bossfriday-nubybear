package cn.bossfriday.fileserver.actors.module;

import cn.bossfriday.fileserver.common.enums.OperationResult;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileUploadResult
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResult {

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

    public FileUploadResult(String fileTransactionId, OperationResult result) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
    }
}

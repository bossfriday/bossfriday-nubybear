package cn.bossfriday.im.common.message.file;

import cn.bossfriday.im.common.entity.file.MetaDataIndex;
import cn.bossfriday.im.common.enums.file.OperationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileUploadOutput
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadOutput {

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

    public FileUploadOutput(String fileTransactionId, OperationResult result) {
        this.fileTransactionId = fileTransactionId;
        this.result = result;
    }
}

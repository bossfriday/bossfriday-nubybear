package cn.bossfriday.fileserver.actors.model;

import cn.bossfriday.fileserver.common.enums.OperationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileDeleteResult
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDeleteResult {

    /**
     * fileTransactionId
     */
    private String fileTransactionId;

    /**
     * result
     */
    private OperationResult result;
}

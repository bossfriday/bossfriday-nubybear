package cn.bossfriday.im.common.message.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeleteTmpFileInput
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteTmpFileInput {

    /**
     * storageEngineVersion
     */
    private int storageEngineVersion;

    /**
     * fileTransactionId
     */
    private String fileTransactionId;
}

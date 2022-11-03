package cn.bossfriday.fileserver.actors.module;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeleteTmpFileMsg
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteTmpFileMsg {

    /**
     * storageEngineVersion
     */
    private int storageEngineVersion;

    /**
     * fileTransactionId
     */
    private String fileTransactionId;
}

package cn.bossfriday.im.common.message.file;

import cn.bossfriday.im.common.entity.file.MetaDataIndex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileDeleteInput
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDeleteInput {

    /**
     * fileTransactionId
     */
    private String fileTransactionId;

    /**
     * metaDataIndex
     */
    private MetaDataIndex metaDataIndex;
}

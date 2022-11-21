package cn.bossfriday.fileserver.actors.model;

import cn.bossfriday.fileserver.engine.model.MetaDataIndex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileDeleteMsg
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDeleteMsg {

    /**
     * fileTransactionId
     */
    private String fileTransactionId;

    /**
     * metaDataIndex
     */
    private MetaDataIndex metaDataIndex;
}

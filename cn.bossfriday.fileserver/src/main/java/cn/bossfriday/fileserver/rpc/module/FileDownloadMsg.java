package cn.bossfriday.fileserver.rpc.module;

import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileDownloadMsg
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadMsg {

    /**
     * fileTransactionId
     */
    private String fileTransactionId;

    /**
     * metaDataIndex
     */
    private MetaDataIndex metaDataIndex;

    /**
     * fileTotalSize
     */
    private long fileTotalSize;

    /**
     * chunkIndex
     */
    private long chunkIndex;
}

package cn.bossfriday.fileserver.actors.module;

import cn.bossfriday.fileserver.engine.entity.MetaData;
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


    public static final long FIRST_CHUNK_INDEX = 0L;

    /**
     * fileTransactionId
     */
    private String fileTransactionId;

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
}

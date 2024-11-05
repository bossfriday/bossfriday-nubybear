package cn.bossfriday.fileserver.actors.model;

import cn.bossfriday.im.common.entity.file.MetaData;
import cn.bossfriday.im.common.entity.file.MetaDataIndex;
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

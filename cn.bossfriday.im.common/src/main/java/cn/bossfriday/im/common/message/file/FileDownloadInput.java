package cn.bossfriday.im.common.message.file;

import cn.bossfriday.im.common.entity.file.MetaData;
import cn.bossfriday.im.common.entity.file.MetaDataIndex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileDownloadInput
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadInput {


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

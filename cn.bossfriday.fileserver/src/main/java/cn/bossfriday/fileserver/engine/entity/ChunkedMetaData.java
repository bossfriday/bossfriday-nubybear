package cn.bossfriday.fileserver.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ChunkedMetaData
 *
 * @author chenx
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChunkedMetaData {

    /**
     * metaData
     */
    private MetaData metaData;

    /**
     * position
     */
    private long position;

    /**
     * chunkedData
     */
    private byte[] chunkedData;
}

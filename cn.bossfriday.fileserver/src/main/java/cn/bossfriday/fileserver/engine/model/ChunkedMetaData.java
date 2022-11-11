package cn.bossfriday.fileserver.engine.model;

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
     * offset
     */
    private long offset;

    /**
     * chunkedData
     */
    private byte[] chunkedData;
}

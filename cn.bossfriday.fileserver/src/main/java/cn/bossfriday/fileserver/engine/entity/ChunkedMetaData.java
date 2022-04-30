package cn.bossfriday.fileserver.engine.entity;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ChunkedMetaData {
    @Getter
    private MetaData metaData;

    @Getter
    private long position;

    @Getter
    private byte[] chunkedData;

    public ChunkedMetaData(MetaData metaData, long position, byte[] chunkedData) {
        this.metaData = metaData;
        this.position = position;
        this.chunkedData = chunkedData;
    }
}

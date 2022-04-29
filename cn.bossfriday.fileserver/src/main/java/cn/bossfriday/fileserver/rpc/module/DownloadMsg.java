package cn.bossfriday.fileserver.rpc.module;

import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadMsg {
    private String fileTransactionId;
    private MetaDataIndex metaDataIndex;
    private long fileTotalSize;
    private long chunkIndex;

    public DownloadMsg() {

    }

    public DownloadMsg(String fileTransactionId, MetaDataIndex metaDataIndex, long fileTotalSize, long chunkIndex) {
        this.fileTransactionId = fileTransactionId;
        this.metaDataIndex = metaDataIndex;
        this.fileTotalSize = fileTotalSize;
        this.chunkIndex = chunkIndex;
    }
}

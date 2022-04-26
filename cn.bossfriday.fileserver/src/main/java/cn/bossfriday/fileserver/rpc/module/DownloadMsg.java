package cn.bossfriday.fileserver.rpc.module;

import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadMsg {
//    private String fileTransactionId;
//    private boolean isKeepAlive;
//    private String clusterNode;
//    private int storeEngineVersion;
//    private String namespace;
//    private long timestamp;
//    private long offset;
//    private int chunkIndex;
//    private int chunkSize;
//    private int chunkCount;
//    private long fileTotalSize;
//    private String fileName;

    private String fileTransactionId;
    private boolean isKeepAlive;
    private MetaDataIndex metaDataIndex;
    private long chunkIndex;

    public DownloadMsg() {

    }

    public DownloadMsg(String fileTransactionId, boolean isKeepAlive, MetaDataIndex metaDataIndex, long chunkIndex) {
        this.fileTransactionId = fileTransactionId;
        this.isKeepAlive = isKeepAlive;
        this.metaDataIndex = metaDataIndex;
        this.chunkIndex = chunkIndex;
    }
}

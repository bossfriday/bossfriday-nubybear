package cn.bossfriday.fileserver.engine.entity;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class RecoverableTmpFile {
    @Getter
    @Setter
    private String fileTransactionId;

    @Getter
    @Setter
    private int storeEngineVersion;

    @Getter
    @Setter
    private String namespace;

    @Getter
    @Setter
    private int time;

    @Getter
    @Setter
    private long offset;

    @Getter
    @Setter
    private long timestamp;

    @Getter
    @Setter
    private String fileName;

    @Getter
    @Setter
    private long fileTotalSize;

    @Getter
    @Setter
    private String filePath;

    public RecoverableTmpFile() {

    }

    public RecoverableTmpFile(String fileTransactionId,
                              int storeEngineVersion,
                              String namespace,
                              int time,
                              long offset,
                              long timestamp,
                              String fileName,
                              long fileTotalSize,
                              String filePath) {
        this.fileTransactionId = fileTransactionId;
        this.storeEngineVersion = storeEngineVersion;
        this.namespace = namespace;
        this.time = time;
        this.offset = offset;
        this.timestamp = timestamp;
        this.fileName = fileName;
        this.fileTotalSize = fileTotalSize;
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}

package cn.bossfriday.fileserver.rpc.module;

import lombok.Data;

@Data
public class WriteTmpFileMsg {
    private int storageEngineVersion;
    private String fileTransactionId;
    private boolean isKeepAlive;
    private String fileName;
    private long fileSize;
    private long fileTotalSize;
    private long offset;
    private byte[] data;
}

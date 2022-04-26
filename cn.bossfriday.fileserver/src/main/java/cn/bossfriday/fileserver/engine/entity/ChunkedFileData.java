package cn.bossfriday.fileserver.engine.entity;

import lombok.Data;

@Data
public class ChunkedFileData {
    private int chunkCount;
    private long fileTotalSize;
    private String fileName;
    private byte[] data;
}

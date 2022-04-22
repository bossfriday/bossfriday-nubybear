package cn.bossfriday.fileserver.engine.entity;

import cn.bossfriday.fileserver.engine.core.ICodec;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class MetaData implements ICodec<MetaData> {
    private byte storeEngineVersion;    // 存储引擎版本
    private byte fileStatus;            // 文件状态标识
    private long timestamp;             // 上传时间戳
    private String fileName;            // 文件名
    private long fileTotalSize;         // 文件大小

    public MetaData() {

    }

    @Override
    public byte[] serialize() throws Exception {
        return new byte[0];
    }

    @Override
    public MetaData deserialize(byte[] bytes) throws Exception {
        return null;
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[100];
    }
}
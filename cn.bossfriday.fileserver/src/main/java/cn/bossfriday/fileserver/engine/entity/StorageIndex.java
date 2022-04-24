package cn.bossfriday.fileserver.engine.entity;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class StorageIndex {
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

    @Override
    public StorageIndex clone() {
        return StorageIndex.builder()
                .storeEngineVersion(this.storeEngineVersion)
                .time(this.time)
                .offset(this.offset)
                .build();
    }

    /**
     * addOffset
     */
    public void addOffset(long length) {
        this.offset += length;
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}

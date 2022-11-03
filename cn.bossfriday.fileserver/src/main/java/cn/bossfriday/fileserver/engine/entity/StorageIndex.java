package cn.bossfriday.fileserver.engine.entity;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * StorageIndex
 *
 * @author chenx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StorageIndex {

    /**
     * storeEngineVersion
     */
    private int storeEngineVersion;

    /**
     * storageNamespace
     */
    private String storageNamespace;

    /**
     * time
     */
    private int time;

    /**
     * offset
     */
    private long offset;

    /**
     * addOffset
     *
     * @param length
     */
    public void addOffset(long length) {
        this.offset += length;
    }

    @Override
    public StorageIndex clone() {
        return StorageIndex.builder()
                .storeEngineVersion(this.storeEngineVersion)
                .time(this.time)
                .offset(this.offset)
                .build();
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}

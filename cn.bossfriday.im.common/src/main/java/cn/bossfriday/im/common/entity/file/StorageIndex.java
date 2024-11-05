package cn.bossfriday.im.common.entity.file;

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

    /**
     * getClonedStorageIndex
     *
     * @return
     */
    public StorageIndex getClonedStorageIndex() {
        return StorageIndex.builder()
                .storeEngineVersion(this.storeEngineVersion)
                .storageNamespace(this.storageNamespace)
                .time(this.time)
                .offset(this.offset)
                .build();
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}

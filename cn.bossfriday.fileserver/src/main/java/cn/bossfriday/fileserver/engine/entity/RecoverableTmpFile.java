package cn.bossfriday.fileserver.engine.entity;

import cn.bossfriday.common.utils.GsonUtil;
import lombok.Getter;
import lombok.Setter;

public class RecoverableTmpFile {
    @Getter
    @Setter
    private MetaDataIndex metaDataIndex;

    @Getter
    @Setter
    private String filePath;

    public RecoverableTmpFile() {

    }

    public RecoverableTmpFile(MetaDataIndex metaDataIndex, String filePath) {
        this.metaDataIndex = metaDataIndex;
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}

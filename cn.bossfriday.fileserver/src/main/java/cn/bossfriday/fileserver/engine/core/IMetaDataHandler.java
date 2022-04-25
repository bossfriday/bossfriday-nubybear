package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;

public interface IMetaDataHandler {

    /**
     * getLength（获取元数据总长度）
     *
     * @param fileName
     * @param fileTotalSize
     * @return
     */
    Long getLength(String fileName, long fileTotalSize) throws Exception;

    /**
     * downloadUrlEncode（元数据索引编码）
     * @param metaDataIndex
     * @return
     * @throws Exception
     */
    String downloadUrlEncode(MetaDataIndex metaDataIndex) throws Exception;

    /**
     * downloadUrlDecode
     * @param input
     * @return
     * @throws Exception
     */
    MetaDataIndex downloadUrlDecode(String input) throws Exception;
}

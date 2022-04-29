package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;

public interface IMetaDataHandler {

    /**
     * getMetaDataLength（获取元数据总长度）
     *
     * @param fileName
     * @param fileTotalSize
     * @return
     */
    Long getMetaDataTotalLength(String fileName, long fileTotalSize) throws Exception;

    /**
     * getMetaDataLength（不包含文件Data）
     * @param fileName
     * @return
     * @throws Exception
     */
    int getMetaDataLength(String fileName) throws Exception;

    /**
     * downloadUrlEncode（元数据索引下载地址编码）
     * @param metaDataIndex
     * @return
     * @throws Exception
     */
    String downloadUrlEncode(MetaDataIndex metaDataIndex) throws Exception;

    /**
     * downloadUrlDecode（元数据索引下载地址解码）
     * @param input
     * @return
     * @throws Exception
     */
    MetaDataIndex downloadUrlDecode(String input) throws Exception;
}

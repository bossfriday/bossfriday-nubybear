package cn.bossfriday.fileserver.engine.core;

import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;

import java.io.IOException;

/**
 * IMetaDataHandler
 *
 * @author chenx
 */
public interface IMetaDataHandler {

    /**
     * getMetaDataTotalLength（获取元数据总长度）
     *
     * @param fileName
     * @param fileTotalSize
     * @return
     */
    Long getMetaDataTotalLength(String fileName, long fileTotalSize);

    /**
     * getMetaDataLength（不包含文件Data）
     *
     * @param fileName
     * @return
     */
    int getMetaDataLength(String fileName);

    /**
     * downloadUrlEncode（元数据索引下载地址编码）
     *
     * @param metaDataIndex
     * @return
     * @throws IOException
     */
    String downloadUrlEncode(MetaDataIndex metaDataIndex) throws IOException;

    /**
     * downloadUrlDecode（元数据索引下载地址解码）
     *
     * @param input
     * @return
     * @throws IOException
     */
    MetaDataIndex downloadUrlDecode(String input) throws IOException;
}

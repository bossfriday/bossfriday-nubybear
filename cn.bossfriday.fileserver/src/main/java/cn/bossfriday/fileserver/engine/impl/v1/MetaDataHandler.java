package cn.bossfriday.fileserver.engine.impl.v1;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.Base58Util;
import cn.bossfriday.fileserver.engine.core.CurrentStorageEngineVersion;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.model.MetaDataIndex;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static cn.bossfriday.fileserver.common.FileServerConst.URL_PREFIX_STORAGE_VERSION;
import static cn.bossfriday.fileserver.common.FileServerConst.URL_RESOURCE;
import static cn.bossfriday.fileserver.engine.model.MetaData.*;
import static cn.bossfriday.fileserver.engine.model.MetaDataIndex.HASH_CODE_LENGTH;

/**
 * MetaDataHandler
 *
 * @author chenx
 */
@Slf4j
@CurrentStorageEngineVersion
public class MetaDataHandler implements IMetaDataHandler {

    @Override
    public Long getMetaDataTotalLength(String fileName, long fileTotalSize) {
        return this.getMetaDataLength(fileName) + fileTotalSize;
    }

    @Override
    public int getMetaDataLength(String fileName) {
        /**
         * storeEngineVersion 1字节
         * fileStatus 1字节
         * timestamp 8字节
         * fileName utf8字符串
         * fileTotalSize 8字节
         */
        return STORE_ENGINE_VERSION_LENGTH
                + FILE_STATUS_LENGTH
                + TIMESTAMP_LENGTH
                + UTF8_FIRST_SIGNIFICANT_LENGTH + fileName.getBytes(StandardCharsets.UTF_8).length
                + FILE_TOTAL_SIZE_LENGTH;
    }

    @Override
    public String downloadUrlEncode(MetaDataIndex metaDataIndex) throws IOException {
        byte[] bytes = metaDataIndex.serialize();
        obfuscateMetaDataIndex(bytes);
        String metaDataIndexString = Base58Util.encode(bytes);

        // 使用Content-Disposition头保障原文件名下载
        return "/" + URL_RESOURCE + "/" + URL_PREFIX_STORAGE_VERSION + metaDataIndex.getStoreEngineVersion() + "/" + metaDataIndexString;
    }

    @Override
    public MetaDataIndex downloadUrlDecode(String input) throws IOException {
        byte[] bytes = Base58Util.decode(input);
        obfuscateMetaDataIndex(bytes);

        return new MetaDataIndex().deserialize(bytes);
    }

    /**
     * obfuscateMetaDataIndex 混淆MetaDataIndex
     *
     * @param bytes
     */
    private static void obfuscateMetaDataIndex(byte[] bytes) {
        if (bytes == null) {
            throw new BizException("bytes is null");
        }

        if (bytes.length <= HASH_CODE_LENGTH) {
            throw new BizException("bytes.length <= " + HASH_CODE_LENGTH);
        }

        int leftBytesSize = bytes.length - HASH_CODE_LENGTH;
        byte[] hashBytes = new byte[HASH_CODE_LENGTH];
        byte[] leftBytes = new byte[leftBytesSize];

        System.arraycopy(bytes, 0, hashBytes, 0, HASH_CODE_LENGTH);
        System.arraycopy(bytes, HASH_CODE_LENGTH, leftBytes, 0, leftBytesSize);
        for (int i = HASH_CODE_LENGTH; i < bytes.length; i++) {
            bytes[i] = (byte) (hashBytes[i % HASH_CODE_LENGTH] ^ leftBytes[i - HASH_CODE_LENGTH]);
        }
    }
}

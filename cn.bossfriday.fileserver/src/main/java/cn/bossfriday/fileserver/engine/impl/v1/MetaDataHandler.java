package cn.bossfriday.fileserver.engine.impl.v1;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.Base58Util;
import cn.bossfriday.fileserver.engine.core.CurrentStorageEngineVersion;
import cn.bossfriday.fileserver.engine.core.IMetaDataHandler;
import cn.bossfriday.fileserver.engine.entity.MetaDataIndex;
import lombok.extern.slf4j.Slf4j;

import static cn.bossfriday.fileserver.engine.entity.MetaDataIndex.HASH_CODE_LENGTH;

@Slf4j
@CurrentStorageEngineVersion
public class MetaDataHandler implements IMetaDataHandler {

    @Override
    public Long getLength(String fileName, long fileTotalSize) throws Exception {
        return 1L + 1L + 8L + fileName.getBytes("UTF-8").length + fileTotalSize;
    }

    @Override
    public String downloadUrlEncode(MetaDataIndex metaDataIndex) throws Exception {
        byte[] bytes = metaDataIndex.serialize();
        obfuscateMetaDataIndex(bytes);

        return Base58Util.encode(bytes);
    }

    @Override
    public MetaDataIndex downloadUrlDecode(String input) throws Exception {
        byte[] bytes = Base58Util.decode(input);
        obfuscateMetaDataIndex(bytes);
        MetaDataIndex metaDataIndex = new MetaDataIndex().deserialize(bytes);

        return metaDataIndex;
    }

    /**
     * 混淆 MetaDataIndex
     */
    private static void obfuscateMetaDataIndex(byte[] bytes) throws Exception {
        byte[] hashBytes = null;
        byte[] leftBytes = null;
        try {
            if (bytes == null) {
                throw new BizException("bytes is null");
            }

            if (bytes.length <= HASH_CODE_LENGTH) {
                throw new BizException("bytes.length <= " + HASH_CODE_LENGTH);
            }

            int leftBytesSize = bytes.length - HASH_CODE_LENGTH;
            hashBytes = new byte[HASH_CODE_LENGTH];
            leftBytes = new byte[leftBytesSize];

            System.arraycopy(bytes, 0, hashBytes, 0, HASH_CODE_LENGTH);
            System.arraycopy(bytes, HASH_CODE_LENGTH, leftBytes, 0, leftBytesSize);
            for (int i = HASH_CODE_LENGTH; i < bytes.length; i++) {
                bytes[i] = (byte) (hashBytes[i % HASH_CODE_LENGTH] ^ leftBytes[i - HASH_CODE_LENGTH]);
            }
        } finally {
            hashBytes = null;
            leftBytes = null;
        }
    }
}

package cn.bossfriday.fileserver.engine.impl.v1;

import cn.bossfriday.common.utils.FileUtil;
import cn.bossfriday.common.utils.LRUHashMap;
import cn.bossfriday.common.utils.RandomAccessFileBuffer;
import cn.bossfriday.fileserver.context.FileTransactionContext;
import cn.bossfriday.fileserver.context.FileTransactionContextManager;
import cn.bossfriday.fileserver.engine.StorageEngine;
import cn.bossfriday.fileserver.engine.core.ITmpFileHandler;
import cn.bossfriday.fileserver.engine.core.StorageEngineVersion;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileMsg;
import cn.bossfriday.fileserver.rpc.module.WriteTmpFileResp;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static cn.bossfriday.fileserver.common.FileServerConst.FILE_UPLOADING_TMP_FILE_EXT;

@Slf4j
@StorageEngineVersion
public class TmpFileHandler implements ITmpFileHandler {
    private LRUHashMap<String, RandomAccessFileBuffer> tmpFileAccessMap = new LRUHashMap<>(10000, null, 1000 * 60 * 60L * 2);

    @Override
    public WriteTmpFileResp write(WriteTmpFileMsg msg) throws Exception {
        String fileTransactionId = msg.getFileTransactionId();
        FileTransactionContext ctx = FileTransactionContextManager.getInstance().getContext(fileTransactionId);
        if (ctx == null)
            throw new Exception("FileTransactionContext is null!(" + fileTransactionId + ")");

        RandomAccessFileBuffer tmpFileAccess = getTmpFileAccess(msg);
        tmpFileAccess.write(msg.getOffset(), msg.getData());
        ctx.addTmpFileSaveSize(msg.getData().length);

        if(ctx.isFlushTmpFile()) {
            tmpFileAccess.flush();
        }

        if (ctx.isCloseTmpFileAccess()) {
            tmpFileAccess.close();
            tmpFileAccessMap.remove(fileTransactionId);
            log.info("tmpFile saved done :" + fileTransactionId);
        }

        return new WriteTmpFileResp();
    }

    /**
     * getTmpFile
     */
    private synchronized RandomAccessFileBuffer getTmpFileAccess(WriteTmpFileMsg msg) throws Exception {
        String fileTransactionId = msg.getFileTransactionId();
        if (tmpFileAccessMap.containsKey(fileTransactionId))
            return tmpFileAccessMap.get(fileTransactionId);

        File tmpDir = StorageEngine.getInstance().getTmpDir();
        String tmpFileName = fileTransactionId + "." + FILE_UPLOADING_TMP_FILE_EXT;
        File tmpFile = new File(tmpDir, tmpFileName);

        if (!tmpFile.exists())
            FileUtil.create(tmpFile,msg.getFileSize());

        RandomAccessFileBuffer raf = new RandomAccessFileBuffer(tmpFile);
        tmpFileAccessMap.put(fileTransactionId, raf);

        return raf;
    }


}

package cn.bossfriday.fileserver.context;

import cn.bossfriday.common.exception.BizException;
import cn.bossfriday.common.utils.LruHashMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import static cn.bossfriday.fileserver.common.FileServerConst.STORAGE_FILE_CHANNEL_LRU_DURATION;

/**
 * FileTransactionContextManager
 *
 * @author chenx
 */
@Slf4j
public class FileTransactionContextManager {

    private LruHashMap<String, FileTransactionContext> contextMap;
    private static volatile FileTransactionContextManager instance = null;

    /**
     * 使用LRUHashMap兜底保障不至于出现FileTransactionContext未注销导致的OOM
     */
    private FileTransactionContextManager() {
        this.contextMap = new LruHashMap<>(10000, null, STORAGE_FILE_CHANNEL_LRU_DURATION);
    }

    /**
     * getInstance
     */
    public static FileTransactionContextManager getInstance() {
        if (instance == null) {
            synchronized (FileTransactionContextManager.class) {
                if (instance == null) {
                    instance = new FileTransactionContextManager();
                }
            }
        }

        return instance;
    }

    /**
     * getContext
     *
     * @param fileTransactionId
     * @return
     */
    public FileTransactionContext getContext(String fileTransactionId) {
        if (!this.contextMap.containsKey(fileTransactionId)) {
            throw new BizException("FileTransactionContext not registered!");
        }

        return this.contextMap.get(fileTransactionId);
    }

    /**
     * existed
     *
     * @param fileTransactionId
     * @return
     */
    public boolean existed(String fileTransactionId) {
        return this.contextMap.containsKey(fileTransactionId);
    }

    /**
     * registerContext
     *
     * @param fileTransactionId
     * @param ctx
     * @param isKeepAlive
     * @param userAgent
     */
    public void registerContext(String fileTransactionId, ChannelHandlerContext ctx, boolean isKeepAlive, String userAgent) {
        if (StringUtils.isEmpty(fileTransactionId)) {
            throw new BizException("fileTransactionId is null or empty!");
        }

        // 断点上传为同一个fileTransactionId下的多次不同的Http请求
        FileTransactionContext context = this.contextMap.containsKey(fileTransactionId) ? this.contextMap.get(fileTransactionId) : new FileTransactionContext();
        context.setFileTransactionId(fileTransactionId);
        context.setCtx(ctx);
        context.setKeepAlive(isKeepAlive);
        context.setUserAgent(userAgent);

        this.contextMap.put(fileTransactionId, context);
        log.info("register FileTransactionContext done: " + fileTransactionId);
    }

    /**
     * unregisterContext
     *
     * @param fileTransactionId
     */
    public void unregisterContext(String fileTransactionId) {
        if (StringUtils.isEmpty(fileTransactionId)) {
            log.warn("fileTransactionId is null or empty!");
            return;
        }

        FileTransactionContext context = null;
        try {
            if (this.existed(fileTransactionId)) {
                context = this.contextMap.get(fileTransactionId);
                this.contextMap.remove(fileTransactionId);
                log.info("remove context done: " + fileTransactionId);
            }
        } finally {
            if (context != null) {
                context = null;
            }
        }
    }
}

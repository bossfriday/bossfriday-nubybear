package cn.bossfriday.fileserver.context;

import cn.bossfriday.common.exception.BizException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * FileTransactionContextManager
 *
 * @author chenx
 */
@Slf4j
public class FileTransactionContextManager {

    private ConcurrentHashMap<String, FileTransactionContext> contextMap;
    private static volatile FileTransactionContextManager instance = null;

    private FileTransactionContextManager() {
        this.contextMap = new ConcurrentHashMap<>();
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
            return null;
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
     * addContext
     *
     * @param fileTransactionId
     * @param ctx
     * @param isKeepAlive
     * @param userAgent
     */
    public void addContext(String fileTransactionId, ChannelHandlerContext ctx, boolean isKeepAlive, String userAgent) {
        if (StringUtils.isEmpty(fileTransactionId)) {
            throw new BizException("fileTransactionId is null or empty!");
        }

        if (this.contextMap.containsKey(fileTransactionId)) {
            throw new BizException("duplicated FileTransactionContext!(fileTransactionId:" + fileTransactionId + ")");
        }

        FileTransactionContext context = new FileTransactionContext();
        context.setFileTransactionId(fileTransactionId);
        context.setCtx(ctx);
        context.setKeepAlive(isKeepAlive);
        context.setUserAgent(userAgent);

        this.contextMap.put(fileTransactionId, context);
        log.info("add context done: " + fileTransactionId);
    }

    /**
     * removeContext
     *
     * @param fileTransactionId
     */
    public void removeContext(String fileTransactionId) {
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

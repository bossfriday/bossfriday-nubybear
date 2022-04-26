package cn.bossfriday.fileserver.context;

import cn.bossfriday.common.exception.BizException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FileTransactionContextManager {
    private ConcurrentHashMap<String, FileTransactionContext> contextMap;
    private volatile static FileTransactionContextManager instance = null;

    private FileTransactionContextManager() {
        contextMap = new ConcurrentHashMap<>();
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
     */
    public FileTransactionContext getContext(String fileTransactionId) {
        if (!contextMap.containsKey(fileTransactionId))
            return null;

        return contextMap.get(fileTransactionId);
    }

    /**
     * existed
     */
    public boolean existed(String fileTransactionId) {
        return contextMap.containsKey(fileTransactionId);
    }

    /**
     * addContext
     */
    public void addContext(String fileTransactionId, ChannelHandlerContext ctx, boolean isKeepAlive) throws Exception {
        if (StringUtils.isEmpty(fileTransactionId))
            throw new BizException("fileTransactionId is null or empty!");

        if (contextMap.containsKey(fileTransactionId))
            throw new Exception("duplicated FileTransactionContext!(fileTransactionId:" + fileTransactionId + ")");

        FileTransactionContext context = new FileTransactionContext();
        context.setFileTransactionId(fileTransactionId);
        context.setCtx(ctx);
        context.setKeepAlive(isKeepAlive);

        contextMap.put(fileTransactionId, context);
        log.info("add context done: " + fileTransactionId);
    }

    /**
     * removeContext
     */
    public void removeContext(String fileTransactionId) {
        if (StringUtils.isEmpty(fileTransactionId)) {
            log.warn("fileTransactionId is null or empty!");
            return;
        }

        if (existed(fileTransactionId)) {
            contextMap.remove(fileTransactionId);
            log.info("remove context done: " + fileTransactionId);
        }
    }
}

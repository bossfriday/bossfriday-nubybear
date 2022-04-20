package cn.bossfriday.fileserver.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

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
    public FileTransactionContext getContext(String fileTransactionId) throws Exception {
        if (!contextMap.containsKey(fileTransactionId)) {
            throw new Exception("FileTransactionContext not existed: " + fileTransactionId);
        }

        return contextMap.get(fileTransactionId);
    }

    /**
     * addContext
     */
    public void addContext(int  version,String fileTransactionId, ChannelHandlerContext ctx, long fileSize, long fileTotalSize) throws Exception {
        if (contextMap.containsKey(fileTransactionId))
            throw new Exception("duplicated FileTransactionContext!(fileTransactionId:" + fileTransactionId + ")");

        FileTransactionContext context = new FileTransactionContext();
        context.setStorageEngineVersion(version);
        context.setFileTransactionId(fileTransactionId);
        context.setCtx(ctx);
        context.setFileSize(fileSize);
        context.setFileTotalSize(fileTotalSize);

        contextMap.put(fileTransactionId, context);
    }

    /**
     * removeContext
     */
    public void removeContext(String fileTransactionId) {
        contextMap.remove(fileTransactionId);
    }
}

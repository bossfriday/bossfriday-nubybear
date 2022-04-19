package cn.bossfriday.fileserver.context;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicLong;

public class FileTransactionContext {
    @Getter
    @Setter
    private String fileTransactionId;

    @Getter
    @Setter
    private ChannelHandlerContext ctx;

    @Getter
    @Setter
    private Long fileSize;

    @Getter
    @Setter
    private Long fileTotalSize;

    @Getter
    @Setter
    protected boolean isKeepAlive;

    private AtomicLong tmpFileSaveSize = new AtomicLong(0);

    /**
     * addTmpFileSaveSize
     */
    public void addTmpFileSaveSize(int size) {
        tmpFileSaveSize.addAndGet(size);
    }

    /**
     * isCloseTmpFileAccess
     */
    public boolean isCloseTmpFileAccess() {
        return tmpFileSaveSize.get() >= fileTotalSize;
    }

    /**
     * isFlushTmpFile
     */
    public boolean isFlushTmpFile() {
        return tmpFileSaveSize.get() >= fileSize;
    }
}

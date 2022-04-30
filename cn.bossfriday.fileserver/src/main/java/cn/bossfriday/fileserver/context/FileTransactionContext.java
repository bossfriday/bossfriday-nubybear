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
    private boolean isKeepAlive;

    private AtomicLong transferredSize = new AtomicLong(0);

    /**
     * addAndGetTransferredSize
     */
    public Long addAndGetTransferredSize(int size) {
        return transferredSize.addAndGet(size);
    }
}

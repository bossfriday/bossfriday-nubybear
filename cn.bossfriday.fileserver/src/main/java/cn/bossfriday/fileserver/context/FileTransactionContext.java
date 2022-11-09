package cn.bossfriday.fileserver.context;

import io.netty.channel.ChannelHandlerContext;
import lombok.*;

import java.util.concurrent.atomic.AtomicLong;

/**
 * FileTransactionContext
 *
 * @author chenx
 */
@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileTransactionContext {

    private String fileTransactionId;

    private ChannelHandlerContext ctx;

    private boolean isKeepAlive;

    private String userAgent;

    private AtomicLong tempFileWriteIndex = new AtomicLong(0);

    /**
     * addAndGetTempFileWriteIndex
     *
     * @param size
     * @return
     */
    public Long addAndGetTempFileWriteIndex(int size) {
        return this.tempFileWriteIndex.addAndGet(size);
    }
}

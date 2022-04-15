package cn.bossfriday.fileserver.http;

import cn.bossfriday.common.utils.UUIDUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpFileServerHandler extends ChannelInboundHandlerAdapter {
    private HttpRequest request;
    private HttpPostRequestDecoder decoder;
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(false);
    private String fileTransactionId;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof HttpRequest) {
                HttpRequest request = this.request = (HttpRequest) msg;
                if (HttpMethod.GET.equals(request.method())) {
                    // todo: GET文件下载
                } else if (HttpMethod.POST.equals(request.method())) {
                    // POST文件上传
                    fileTransactionId = UUIDUtil.getShortString();
                    decoder = new HttpPostRequestDecoder(factory, request);
                }
            }

            if (msg instanceof HttpObject) {
                fileUpload(ctx, (HttpObject) msg);
            }
        } catch (Exception ex) {
            log.error("HttpFileServerHandler error!", ex);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        log.error("HttpFileServerHandler.exceptionCaught()", cause);
    }

    /**
     * 文件上传
     */
    private void fileUpload(ChannelHandlerContext ctx, HttpObject msg) {
        if (decoder == null) {
            return;
        }

        try {
            if (msg instanceof HttpContent) {
                HttpContent chunk = (HttpContent) msg;
                decoder.offer(chunk);

                chunkedReadHttpData();

                if (chunk instanceof LastHttpContent) {
                    release();
                }
            }
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.fileUpload() error!", ex);
            release();
        }
    }

    /**
     * 分片读取数据
     */
    private void chunkedReadHttpData() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    HttpData httpData = (HttpData) data;
                    if (httpData instanceof FileUpload) {
                        chunkedProcessHttpData(httpData);
                    }
                }
            }

            HttpData data = (HttpData) decoder.currentPartialHttpData();
            if (data != null && data instanceof FileUpload) {
                chunkedProcessHttpData(data);
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            log.info("end of content chunk by chunk, ");
        } catch (Throwable tr) {
            log.error("HttpFileServerHandler.readHttpDataChunkByChunk() error!", tr);
        }
    }

    /**
     * 分片处理数据
     */
    private void chunkedProcessHttpData(HttpData data) {
        try {
            ByteBuf byteBuf = data.getByteBuf();
            int readBytesCount = byteBuf.readableBytes();
            byteBuf.readBytes(byteBuf.readableBytes());
            System.out.println(fileTransactionId + "---->read bytes:" + readBytesCount);
        } catch (Exception ex) {
            log.error("HttpFileServerHandler.writeHttpData() error!", ex);
        }
    }

    /**
     * 释放所有资源
     */
    private void release() {
        try {
            request = null;
            if (decoder != null) {
                decoder.destroy();
                decoder = null;
            }
        } catch (Exception e) {
            log.error("HttpFileServerHandler.release() error!", e);
        }
    }
}

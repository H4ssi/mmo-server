package mmo.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import mmo.server.Clock.Callback;

import javax.inject.Inject;

public class NotificationHandler extends ChannelInboundHandlerAdapter {

    private Callback cb;
    private final Clock clock;

    @Inject
    public NotificationHandler(Clock clock) {
        this.clock = clock;
    }

    public void channelRead(final ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpRequest) {
            HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK);

            // this is needed for browsers to render content as soon as it is
            // received
            HttpHeaders.setHeader(res, HttpHeaders.Names.CONTENT_TYPE,
                    "text/html; charset=utf-8");
            HttpHeaders.setKeepAlive(res, false);
            HttpHeaders.setTransferEncodingChunked(res);

            ctx.writeAndFlush(res);

            final ByteBuf i = Unpooled
                    .wrappedBuffer("<!DOCTYPE html><html><body><pre>\n"
                            .getBytes(CharsetUtil.UTF_8));
            ctx.writeAndFlush(new DefaultHttpContent(i));

            cb = new Callback() {

                @Override
                public void tock() {
                    ctx.writeAndFlush(new DefaultHttpContent(
                            Unpooled.wrappedBuffer("tock\n"
                                    .getBytes(CharsetUtil.UTF_8))));
                }

                @Override
                public void tick() {
                    ctx.writeAndFlush(new DefaultHttpContent(
                            Unpooled.wrappedBuffer("tick\n"
                                    .getBytes(CharsetUtil.UTF_8))));
                }
            };
            clock.addCallback(cb);
        } else if (msg instanceof LastHttpContent) {
            System.out.println("client end of data");
        } else if (msg instanceof HttpContent) {
            ctx.writeAndFlush(new DefaultHttpContent(Unpooled
                    .wrappedBuffer("pong".getBytes(CharsetUtil.UTF_8))));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel closed");
        if (cb != null) {
            clock.removeCallback(cb);
            cb = null;
        }
        super.channelInactive(ctx);
    }
}

package mmo.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;

import javax.inject.Inject;
import javax.inject.Provider;

public class Handler extends ChannelInboundHandlerAdapter {
    private ChannelInboundHandler handler = null;

    private final Provider<DefaultHandler> defaultHandlerProvider;
    private final Provider<NotificationHandler> notificationHandlerProvider;

    @Inject
    public Handler(Provider<DefaultHandler> defaultHandlerProvider,
                   Provider<NotificationHandler> notificationHandlerProvider) {
        this.defaultHandlerProvider = defaultHandlerProvider;
        this.notificationHandlerProvider = notificationHandlerProvider;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;

            switch (request.getUri()) {
                case "":
                case "/":
                    installHandler(ctx, defaultHandlerProvider.get());
                    break;
                case "/game":
                    installHandler(ctx, notificationHandlerProvider.get());
                    break;
                default:
                    installHandler(ctx, null);
            }
        }

        if (handler != null) {
            super.channelRead(ctx, msg);
        } else {
            ctx.writeAndFlush(create404());
            ReferenceCountUtil.release(msg);
        }
    }

    private void installHandler(ChannelHandlerContext ctx,
                                ChannelInboundHandler newHandler) {
        if (handler != null) {
            ctx.pipeline().removeLast();
            handler = null;
        }
        if (newHandler != null) {
            ctx.pipeline().addLast(newHandler);
            handler = newHandler;
        }
    }

    private FullHttpResponse create404() {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        HttpHeaders.setKeepAlive(response, true);
        HttpHeaders.setContentLength(response, 0);

        return response;
    }
}
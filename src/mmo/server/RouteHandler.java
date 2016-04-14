/*
 * Copyright 2015 Florian Hassanen
 *
 * This file is part of mmo-server.
 *
 * mmo-server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * mmo-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with mmo-server.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package mmo.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.ReferenceCountUtil;
import mmo.server.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URI;
import java.util.regex.Pattern;

@ChannelHandler.Sharable
public class RouteHandler extends ChannelInboundHandlerAdapter {
    private static final Logger L = LoggerFactory.getLogger(RouteHandler.class);

    private final DefaultHandlerFactory defaultHandlerFactory;
    private final StatusHandlerFactory statusHandlerFactory;
    private final RoomHandlerFactory roomHandlerFactory;
    private final Provider<WebSocketMessageHandler> webSocketMessageHandlerProvider;
    private final MessageReceiverFactory messageReceiverFactory;

    private static final Pattern PATH_SEP = Pattern.compile(Pattern.quote("/"));

    @Inject
    public RouteHandler(DefaultHandlerFactory defaultHandlerFactory,
                        StatusHandlerFactory statusHandlerFactory,
                        RoomHandlerFactory roomHandlerFactory,
                        Provider<WebSocketMessageHandler> webSocketMessageHandlerProvider,
                        MessageReceiverFactory messageReceiverFactory) {
        this.defaultHandlerFactory = defaultHandlerFactory;
        this.statusHandlerFactory = statusHandlerFactory;
        this.roomHandlerFactory = roomHandlerFactory;
        this.webSocketMessageHandlerProvider = webSocketMessageHandlerProvider;
        this.messageReceiverFactory = messageReceiverFactory;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;

            String[] path = PATH_SEP.split(request.getUri(), -1);
            for (int i = 0; i < path.length; ++i) {
                path[i] = URI.create(path[i]).getPath();
            }

            String first = path.length == 1 ? "" : path[1];

            switch (first) {
                case "":
                    installHandlers(ctx, defaultHandlerFactory.create());
                    break;
                case "status":
                    installHandlers(ctx, statusHandlerFactory.create());
                    break;
                case "room":
                    int roomId;
                    if (path.length < 3) {
                        roomId = 0;
                    } else {
                        try {
                            roomId = Integer.parseInt(path[2]);
                        } catch (NumberFormatException e) {
                            roomId = 0;
                        }
                    }
                    installHandlers(ctx, roomHandlerFactory.create(roomId));
                    break;
                case "game":
                    installWebSocketHandler(ctx, path.length > 2 ? path[2] : null, request.getUri(), msg);
                    return; // request was upgraded, do not handle it any further
                default:
                    installHandlers(ctx);
            }
        }

        if (hasNextHandler(ctx)) {
            super.channelRead(ctx, msg);
        } else {
            ctx.writeAndFlush(create404());
            ReferenceCountUtil.release(msg);
        }
    }

    private void installWebSocketHandler(ChannelHandlerContext ctx, String playerName, String uri, Object msg) {
        L.debug("upgrading request {} to websocket", uri);

        if (playerName == null || playerName.isEmpty()) {
            playerName = "anonymous";
        }

        // remove other handlers
        installHandlers(ctx);

        // add websocket handlers
        ctx.pipeline().addLast(new WebSocketServerProtocolHandler(uri),
                webSocketMessageHandlerProvider.get(),
                messageReceiverFactory.create(new Player(playerName)));

        // initiate handshake
        ctx.fireChannelRead(msg);

        // put _this_ handler at very end now
        ctx.pipeline().remove(this);
        ctx.pipeline().addLast(this);
    }

    private boolean hasNextHandler(ChannelHandlerContext ctx) {
        return this != ctx.pipeline().last();
    }


    private void installHandlers(ChannelHandlerContext ctx,
                                 ChannelInboundHandler... newHandlers) {
        while (hasNextHandler(ctx)) {
            ctx.pipeline().removeLast();
        }

        for (ChannelInboundHandler h : newHandlers) {
            ctx.pipeline().addLast(h);
        }
    }

    private FullHttpResponse create404() {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        HttpHeaders.setKeepAlive(response, true);
        HttpHeaders.setContentLength(response, 0);

        return response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        L.error("route handler exception", cause);
        super.exceptionCaught(ctx, cause);
    }
}
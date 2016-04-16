/*
 * Copyright 2016 Florian Hassanen
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
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import mmo.server.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URI;
import java.util.regex.Pattern;

@ChannelHandler.Sharable
public class RouteHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger L = LoggerFactory.getLogger(RouteHandler.class);

    private final DefaultHandler defaultHandler;
    private final StatusHandler statusHandler;
    private final DataToHttpEncoder dataToHttpEncoder;
    private final PageNotFoundHandler pageNotFoundHandler;
    private final RoomHandlerFactory roomHandlerFactory;
    private final Provider<WebSocketMessageHandler> webSocketMessageHandlerProvider;
    private final MessageReceiverFactory messageReceiverFactory;

    private static final Pattern PATH_SEP = Pattern.compile(Pattern.quote("/"));
    private static final String PAGE_HANDLER_NAME = "current-page-handler";

    private static long uniqueNameId = 0;

    @Inject
    public RouteHandler(DefaultHandler defaultHandler,
                        StatusHandler statusHandler,
                        DataToHttpEncoder dataToHttpEncoder,
                        PageNotFoundHandler pageNotFoundHandler,
                        RoomHandlerFactory roomHandlerFactory,
                        Provider<WebSocketMessageHandler> webSocketMessageHandlerProvider,
                        MessageReceiverFactory messageReceiverFactory) {
        super(false);
        this.defaultHandler = defaultHandler;
        this.statusHandler = statusHandler;
        this.dataToHttpEncoder = dataToHttpEncoder;
        this.pageNotFoundHandler = pageNotFoundHandler;
        this.roomHandlerFactory = roomHandlerFactory;
        this.webSocketMessageHandlerProvider = webSocketMessageHandlerProvider;
        this.messageReceiverFactory = messageReceiverFactory;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().addLast(dataToHttpEncoder);
        ctx.pipeline().addLast(PAGE_HANDLER_NAME, pageNotFoundHandler);
        ctx.pipeline().addLast(new ChannelHandlerAdapter() {
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                L.error("exception", cause);
            }
        });
        super.handlerAdded(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String[] path = PATH_SEP.split(request.getUri(), -1);
        for (int i = 0; i < path.length; ++i) {
            path[i] = URI.create(path[i]).getPath();
        }

        String first = path.length == 1 ? "" : path[1];

        switch (first) {
            case "":
                installHandler(ctx, defaultHandler);
                break;
            case "status":
                installHandler(ctx, statusHandler);
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
                installHandler(ctx, roomHandlerFactory.create(roomId));
                break;
            case "game":
                installWebSocketHandler(ctx, path.length > 2 ? path[2] : null, request.getUri());
                break;
            default:
                installHandler(ctx, pageNotFoundHandler);
        }

        ctx.fireChannelRead(request);
    }

    private void installWebSocketHandler(ChannelHandlerContext ctx, String playerName, String uri) {
        L.debug("upgrading request {} to websocket", uri);

        if (playerName == null || playerName.isEmpty()) {
            playerName = "anonymous";
        }

        // add websocket handlers
        ctx.pipeline()
                .addBefore(PAGE_HANDLER_NAME, generateName("ws-srv"), new WebSocketServerProtocolHandler(uri))
                .addBefore(PAGE_HANDLER_NAME, generateName("ws-msg"), webSocketMessageHandlerProvider.get())
                .addBefore(PAGE_HANDLER_NAME, generateName("player"), messageReceiverFactory.create(new Player(playerName)));
    }

    private void installHandler(ChannelHandlerContext ctx, ChannelInboundHandler newHandler) {
        ctx.pipeline().replace(PAGE_HANDLER_NAME, PAGE_HANDLER_NAME, newHandler);
    }

    private static String generateName(String base) {
        return base + "-" + uniqueNameId++;
    }
}
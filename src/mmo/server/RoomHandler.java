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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.Iterables;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import mmo.server.data.RoomInfo;
import mmo.server.model.Coord;

@AutoFactory
public class RoomHandler extends ChannelInboundHandlerAdapter {
    private final ObjectMapper mapper;
    private final int roomId;
    private final GameLoop gameLoop;

    public RoomHandler(@Provided ObjectMapper mapper,
                       @Provided GameLoop gameLoop,
                       int roomId) {
        this.mapper = mapper;
        this.gameLoop = gameLoop;
        this.roomId = roomId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        try {
            if (msg instanceof HttpRequest) {
                RoomInfo roomInfo = new RoomInfo(roomId,
                        Iterables.toArray(gameLoop.getRoom(roomId)
                                .getObstacles(), Coord.class));

                ByteBuf buf = Unpooled.wrappedBuffer(
                        mapper.writeValueAsBytes(roomInfo));
                HttpResponse res = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
                HttpHeaders.setHeader(res, HttpHeaders.Names.CONTENT_TYPE,
                        "text/plain; encoding=utf-8");
                HttpHeaders.setKeepAlive(res, true);
                HttpHeaders.setContentLength(res, buf.readableBytes());

                ctx.writeAndFlush(res);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}

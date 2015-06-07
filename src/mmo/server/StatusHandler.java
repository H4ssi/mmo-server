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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import mmo.server.data.ServerInfo;

@AutoFactory
public class StatusHandler extends ChannelInboundHandlerAdapter {
    private final ObjectMapper mapper;

    public StatusHandler(@Provided ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        try {
            if (msg instanceof HttpRequest) {
                ServerInfo serverInfo = new ServerInfo("up", "Chuck Norris " +
                        "only needs one (1) pokeball to catch " +
                        "legendery pokemon.");
                ByteBuf buf = Unpooled.wrappedBuffer(
                        mapper.writeValueAsBytes(serverInfo));
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

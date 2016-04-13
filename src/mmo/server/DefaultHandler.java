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

import com.google.auto.factory.AutoFactory;
import com.google.common.io.ByteStreams;
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
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

@AutoFactory
public class DefaultHandler extends ChannelInboundHandlerAdapter {
    private static final Logger L = LoggerFactory.getLogger(DefaultHandler.class);

    private final byte[] data;

    public DefaultHandler() {
        byte data[] = null;
        try (InputStream readmeStream = DefaultHandler.class.getResourceAsStream("README.md")) {
            if (readmeStream != null) {
                data = ByteStreams.toByteArray(readmeStream);
            }
        } catch (IOException e) {
            L.error("README.md could not be read", e);
        }
        if (data == null) {
            data = "README.md not available :-(".getBytes(CharsetUtil.UTF_8);
        }
        this.data = data;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        try {
            if (msg instanceof HttpRequest) {
                ByteBuf buf = Unpooled.wrappedBuffer(data);
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

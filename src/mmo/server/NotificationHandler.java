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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import javax.inject.Inject;

public class NotificationHandler extends ChannelInboundHandlerAdapter {

    private MessageHub.Receiver receiver;

    @Inject
    public NotificationHandler() {
    }

    public void channelRead(final ChannelHandlerContext ctx, Object msg)
            throws Exception {
        try {
            if (msg instanceof HttpRequest) {
                HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK);

                // this is needed for browsers to render content as soon as it
                // is received
                HttpHeaders.setHeader(res, HttpHeaders.Names.CONTENT_TYPE,
                        "text/html; charset=utf-8");
                HttpHeaders.setKeepAlive(res, false);
                HttpHeaders.setTransferEncodingChunked(res);

                ctx.write(res);
                send(ctx, "<!DOCTYPE html><html><body><pre>\n");

                receiver.init();
            } else if (msg instanceof LastHttpContent) {
                System.out.println("client end of data");
            } else if (msg instanceof HttpContent) {
                receiver.receive(((HttpContent) msg).content());
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void send(ChannelHandlerContext ctx, String msg) {
        ctx.writeAndFlush(new DefaultHttpContent(Unpooled
                .wrappedBuffer(msg.getBytes(CharsetUtil.UTF_8))));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel closed");
        receiver.exit();
        super.channelInactive(ctx);
    }

    public void setReceiver(MessageHub.Receiver playerName) {
        this.receiver = playerName;
    }
}

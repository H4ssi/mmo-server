package mmo.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import mmo.server.GameLoop.Callback;
import mmo.server.message.Entered;
import mmo.server.message.Message;
import mmo.server.model.Coord;

import javax.inject.Inject;
import java.util.Map;

public class NotificationHandler extends ChannelInboundHandlerAdapter {

    private Callback cb;
    private final GameLoop gameLoop;
    private final ObjectWriter writer;

    @Inject
    public NotificationHandler(GameLoop gameLoop, ObjectMapper mapper) {
        this.gameLoop = gameLoop;
        writer = mapper.writerFor(Message.class);
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

            send(ctx, "<!DOCTYPE html><html><body><pre>\n");

            cb = new Callback() {
                @Override
                public void tock() {

                }

                @Override
                public void cannotEnter() {
                    send(ctx, "cannot enter\n");
                }

                @Override
                public void endered(Coord coord) {
                    try {
                        send(ctx, writer.writeValueAsString(
                                new Entered(coord.getX(), coord.getY())));
                    } catch (JsonProcessingException e) {
                        // TODO unhandled exception
                        e.printStackTrace();
                    }
                }

                @Override
                public void left(Coord coord) {
                    send(ctx, "left: " + coord + "\n");
                }

                @Override
                public void inRoom(Map<Coord, Callback> inRoom) {
                    send(ctx, "inRoom: " + inRoom.keySet() + "\n");
                }

                @Override
                public void tick() {

                }
            };
            gameLoop.login(cb);
        } else if (msg instanceof LastHttpContent) {
            System.out.println("client end of data");
        } else if (msg instanceof HttpContent) {
            send(ctx, "pong");
        }
    }

    private void send(ChannelHandlerContext ctx, String msg) {
        ctx.writeAndFlush(new DefaultHttpContent(Unpooled
                .wrappedBuffer(msg.getBytes(CharsetUtil.UTF_8))));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel closed");
        if (cb != null) {
            gameLoop.logout(cb);
            cb = null;
        }
        super.channelInactive(ctx);
    }
}

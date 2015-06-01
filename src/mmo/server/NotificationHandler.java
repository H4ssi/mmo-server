package mmo.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import mmo.server.GameLoop.Callback;
import mmo.server.message.*;
import mmo.server.model.Coord;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

public class NotificationHandler extends ChannelInboundHandlerAdapter {

    private Callback cb;
    private final GameLoop gameLoop;
    private final ObjectWriter writer;
    private final ObjectReader reader;

    @Inject
    public NotificationHandler(GameLoop gameLoop, ObjectMapper mapper) {
        this.gameLoop = gameLoop;
        writer = mapper.writerFor(Message.class);
        reader = mapper.reader(Message.class);
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

                ctx.writeAndFlush(res);

                send(ctx, "<!DOCTYPE html><html><body><pre>\n");

                cb = new Callback() {
                    @Override
                    public void tock() {

                    }

                    @Override
                    public void cannotEnter() {
                        sendMessage(ctx, new CannotEnter());
                    }

                    @Override
                    public void endered(Coord coord) {
                        sendMessage(ctx,
                                new Entered(coord.getX(), coord.getY()));
                    }

                    @Override
                    public void left(Coord coord) {
                        sendMessage(ctx, new Left());
                    }

                    @Override
                    public void inRoom(Map<Coord, Callback> inRoom) {
                        Set<Coord> coords = inRoom.keySet();
                        sendMessage(ctx, new InRoom(
                                coords.toArray(new Coord[coords.size()])));
                    }

                    @Override
                    public void chat(String message) {
                        sendMessage(ctx, new Chat(message));
                    }

                    @Override
                    public void tick() {

                    }
                };
                gameLoop.login(cb);
            } else if (msg instanceof LastHttpContent) {
                System.out.println("client end of data");
            } else if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;

                Message m = reader.readValue(
                        new ByteBufInputStream(content.content()));

                if (m instanceof Chat) {
                    gameLoop.chat(((Chat) m).getMessage());
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void sendMessage(ChannelHandlerContext ctx, Message msg) {
        try {
            ctx.writeAndFlush(
                    new DefaultHttpContent(
                            Unpooled.wrappedBuffer(
                                    writer.writeValueAsBytes(msg))));
        } catch (JsonProcessingException e) {
            // TODO unhandled exception
            e.printStackTrace();
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

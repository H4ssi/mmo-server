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
import io.netty.util.*;
import mmo.server.GameLoop.Callback;
import mmo.server.message.*;
import mmo.server.model.Coord;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NotificationHandler extends ChannelInboundHandlerAdapter {

    private Callback cb;
    private final GameLoop gameLoop;
    private final ObjectWriter writer;
    private final ObjectReader reader;

    private final HashedWheelTimer timer;

    @Inject
    public NotificationHandler(GameLoop gameLoop, ObjectMapper mapper, HashedWheelTimer timer) {
        this.gameLoop = gameLoop;
        this.timer = timer;
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

                ctx.write(res);
                send(ctx, "<!DOCTYPE html><html><body><pre>\n");

                welcomeMessages(ctx);

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

    private void welcomeMessages(final ChannelHandlerContext ctx) {
        String[] msgs = new String[]{
                "<big>Welcome to POS1-mmo!</big>",
                "<big><strong>Please use responsibly!</strong></big>",
                "<big><strong>" +
                        "(e.g. do not use it for cheating at tests!!!)" +
                        "</strong></big>",

                "Find code and updates here: " +
                        "<ul>" +
                        "<li>" +
                        "<a href='https://github.com/H4ssi/mmo-server'>" +
                        "https://github.com/H4ssi/mmo-server" +
                        "</a>" +
                        "</li>" +
                        "<li>" +
                        "<a href='https://github.com/H4ssi/mmo-client'>" +
                        "https://github.com/H4ssi/mmo-client" +
                        "</a>" +
                        "</li>" +
                        "</ul>",

                "<small>This server should receive non-breaking " +
                        "updates only!<br>" +
                        "For the latest and greatest use " +
                        "<a href='http://89.110.148.15:8080'>" +
                        "http://89.110.148.15:8080 (port 8080)" +
                        "</a>" +
                        "</small>",

                "<big><strong>hf</strong></big>"};

        int delay = 2;
        for (final String msg : msgs) {
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    sendMessage(ctx, new Chat(msg));
                }
            }, delay, TimeUnit.SECONDS);
            delay += 1;
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

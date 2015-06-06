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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import mmo.server.message.CannotEnter;
import mmo.server.message.Chat;
import mmo.server.message.Entered;
import mmo.server.message.InRoom;
import mmo.server.message.Left;
import mmo.server.message.Message;
import mmo.server.model.Player;
import mmo.server.model.PlayerInRoom;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class MessageHub {
    private final ConcurrentMap<Player, Channel> channels =
            new ConcurrentHashMap<>();

    private final ObjectWriter writer;
    private final ObjectReader reader;

    private final HashedWheelTimer timer;

    private final HtmlCleaner htmlCleaner;

    private final GameLoop gameLoop;

    @Inject
    public MessageHub(ObjectMapper mapper, HashedWheelTimer timer,
                      HtmlCleaner htmlCleaner, GameLoop gameLoop) {
        writer = mapper.writerFor(Message.class);
        reader = mapper.reader(Message.class);

        this.timer = timer;

        this.htmlCleaner = htmlCleaner;

        this.gameLoop = gameLoop;
    }

    public interface Receiver {
        void init();

        void receive(ByteBuf data) throws IOException;

        void exit();
    }

    public Receiver register(final Player player, final Channel channel) {
        channels.putIfAbsent(player, channel);

        final GameLoop.Callback cb = new GameLoop.Callback() {
            @Override
            public void tock() {

            }

            @Override
            public void cannotEnter() {
                sendMessage(channel, new CannotEnter());
            }

            @Override
            public void entered(PlayerInRoom playerInRoom) {
                if (player.getRoomId() == null) { // first playerInRoom msg
                    player.setRoomId(playerInRoom.getId());
                }
                sendMessage(channel, new Entered(playerInRoom));
            }

            @Override
            public void left(int id) {
                sendMessage(channel, new Left(id));
            }

            @Override
            public void inRoom(List<PlayerInRoom> inRoom) {
                sendMessage(channel, new InRoom(inRoom.toArray(
                        new PlayerInRoom[inRoom.size()]
                )));
            }

            @Override
            public void chat(int id, String message) {
                sendMessage(channel, new Chat(id, message));
            }

            @Override
            public void tick() {

            }

            @Override
            public Player getPlayer() {
                return player;
            }
        };

        gameLoop.login(cb);
        return new Receiver() {
            @Override
            public void init() {
                welcomeMessages(channel);
            }

            @Override
            public void receive(ByteBuf data) throws IOException {
                MessageHub.this.receive(player, data);
            }

            @Override
            public void exit() {
                gameLoop.logout(cb);
            }
        };
    }

    public void receive(Player player, ByteBuf data) throws IOException {
        Message m = reader.readValue(
                new ByteBufInputStream(data));

        if (m instanceof Chat) {
            String orig = ((Chat) m).getMessage();
            if (orig != null && !orig.trim().isEmpty()) {
                String clean = htmlCleaner.clean(orig);
                if (clean.isEmpty()) {
                    clean = "[message deleted]";
                }
                gameLoop.chat(player.getRoomId(), clean);
            }
        }
    }

    private void sendMessage(Channel channel, Message msg) {
        try {
            channel.writeAndFlush(
                    new DefaultHttpContent(
                            Unpooled.wrappedBuffer(
                                    writer.writeValueAsBytes(msg))));
        } catch (JsonProcessingException e) {
            // TODO unhandled exception
            e.printStackTrace();
        }
    }

    private void welcomeMessages(final Channel channel) {
        String[] msgs = new String[]{
                "<big>Welcome to POS1-mmo!</big>",
                "<big><strong>Please use responsibly!</strong></big>",

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

                "<small>mmo-server is free software, and you are welcome to " +
                        "redistribute it under the terms of the GNU AGPL: " +
                        "<a href='https://gnu.org/licenses/agpl.html'>" +
                        "https://gnu.org/licenses/agpl.html" +
                        "</a></small>",

                "<small>This server will receive the newest features " +
                        "and will be updated regularly!<br>" +
                        "For the a more stable version use " +
                        "<a href='http://89.110.148.15:33333'>" +
                        "http://89.110.148.15:33333 (port 33333)" +
                        "</a>" +
                        "</small>",

                "<big><strong>hf</strong></big>"};

        int delay = 2;
        for (final String msg : msgs) {
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    sendMessage(channel, new Chat(null, msg));
                }
            }, delay, TimeUnit.SECONDS);
            delay += 1;
        }
    }
}

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
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import mmo.server.message.Chat;
import mmo.server.message.Message;
import mmo.server.model.Player;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@AutoFactory
public class MessageReceiver {
    private final ObjectReader reader;

    private final HashedWheelTimer timer;

    private final HtmlCleaner htmlCleaner;

    private final GameLoop gameLoop;

    private final MessageHub messageHub;

    private final Player player;

    public MessageReceiver(@Provided ObjectMapper mapper,
                           @Provided HashedWheelTimer timer,
                           @Provided HtmlCleaner htmlCleaner,
                           @Provided GameLoop gameLoop,
                           @Provided MessageHub messageHub,
                           Player player) {
        reader = mapper.reader(Message.class);

        this.timer = timer;

        this.htmlCleaner = htmlCleaner;

        this.gameLoop = gameLoop;

        this.messageHub = messageHub;

        this.player = player;
    }

    public void init(Channel channel) {
        messageHub.register(player, channel);
        gameLoop.login(player);
        welcomeMessages(player);
    }

    public void receive(ByteBuf data) throws IOException {
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

    public void exit() {
        gameLoop.logout(player);
        messageHub.unregister(player);
    }

    private void welcomeMessages(final Player player) {
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
                    messageHub.sendMessage(player, new Chat(null,
                            msg));
                }
            }, delay, TimeUnit.SECONDS);
            delay += 1;
        }
    }
}

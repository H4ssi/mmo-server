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
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.util.ReferenceCountUtil;
import mmo.server.message.Message;
import mmo.server.model.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class MessageHub {
    private final ConcurrentMap<Player, Channel> channels =
            new ConcurrentHashMap<>();

    private final ObjectWriter writer;
    private final MessageReceiverFactory receiverFactory;

    @Inject
    public MessageHub(ObjectMapper mapper, MessageReceiverFactory receiverFactory) {
        this.receiverFactory = receiverFactory;
        writer = mapper.writerFor(Message.class);
    }

    public void sendMessage(Player player, Message message) {
        sendMessage(Collections.singleton(player), message);
    }

    public void sendMessage(Set<Player> players, Message message) {
        try {
            HttpContent content = packMassage(message);
            try {
                for (Player player : players) {
                    Channel channel = channels.get(player);
                    if (channel != null) {
                        content.retain();
                        channel.writeAndFlush(content);
                    }
                }
            } finally {
                ReferenceCountUtil.release(content);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // TODO unhandled
        }
    }

    public interface Receiver {
        void init();

        void receive(ByteBuf data) throws IOException;

        void exit();
    }

    public Receiver register(final Player player, final Channel channel) {
        channels.putIfAbsent(player, channel);

        return receiverFactory.create(player);
    }

    private void sendMessage(Channel channel, Message msg) {
        try {
            channel.writeAndFlush(
                    packMassage(msg));
        } catch (JsonProcessingException e) {
            // TODO unhandled exception
            e.printStackTrace();
        }
    }

    private DefaultHttpContent packMassage(Message msg) throws JsonProcessingException {
        return new DefaultHttpContent(
                Unpooled.wrappedBuffer(
                        writer.writeValueAsBytes(msg)));
    }

}

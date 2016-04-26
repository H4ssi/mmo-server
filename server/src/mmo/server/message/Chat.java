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

package mmo.server.message;

/**
 * Denotes a chat message.
 * <p>
 * Chat messages are supposed to be HTML encoded (Thus clients should handle
 * rendering and special characters like `<`, `>` etc. correctly). Client
 * input is sanitized by the server prior to dispatching.
 *
 * @server the server relays some message for the client to receive
 * @client a client sends a chat message to the room (when the message is sent from the client to the server)
 */
public class Chat implements Message {

    /**
     * author local room id, maybe unset if message was not send by any client (e.g. server messages)
     *
     * @server 12
     */
    private Integer id;

    /**
     * chat message
     *
     * @both "Hello there!"
     */
    private String message;

    public Chat() {
    }

    public Chat(Integer id, String message) {
        this.id = id;
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

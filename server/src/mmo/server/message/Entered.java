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

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import mmo.server.model.PlayerInRoom;

/**
 * Denotes information about a player entering a room.
 *
 * @server when a player enters a room, the server sends this out to all current players in the room, even the player
 * just entering, in fact this is the  very first message sent to the client upon entering a room.
 */
public class Entered implements Message {
    @JsonUnwrapped
    private PlayerInRoom playerInRoom;

    public Entered(PlayerInRoom playerInRoom) {
        this.playerInRoom = playerInRoom;
    }

    public PlayerInRoom getPlayerInRoom() {
        return playerInRoom;
    }

    public void setPlayerInRoom(PlayerInRoom playerInRoom) {
        this.playerInRoom = playerInRoom;
    }
}

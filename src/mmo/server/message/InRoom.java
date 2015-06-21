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

import mmo.server.model.PlayerInRoom;

import java.util.Collection;

public class InRoom implements Message {
    private int room;
    private Collection<PlayerInRoom> players;
    private Collection<PlayerInRoom> mobs;

    public InRoom() {
    }

    public InRoom(int room, Collection<PlayerInRoom> players, Collection
            <PlayerInRoom> mobs) {
        this.room = room;
        this.players = players;
        this.mobs = mobs;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public Collection<PlayerInRoom> getCoords() {
        return players;
    }

    public void setCoords(Collection<PlayerInRoom> coords) {
        this.players = coords;
    }

    public Collection<PlayerInRoom> getMobs() {
        return mobs;
    }

    public void setMobs(Collection<PlayerInRoom> mobs) {
        this.mobs = mobs;
    }
}

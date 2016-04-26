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

/**
 * Denotes a mob spawning.
 * <p>
 * Mobs are considered to be enemies, and can be attacked and defeated by the
 * players.
 *
 * @server sent when a mob enters a room.
 */
public class Spawned extends Entered {
    public Spawned(PlayerInRoom playerInRoom) {
        super(playerInRoom);
    }
}

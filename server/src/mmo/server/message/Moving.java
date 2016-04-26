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

import mmo.server.model.Direction;

/**
 * Denotes a player starting to move.
 * <p>
 * Note that the player does still need to finish the move. This is just an
 * indication that the movement was started. The current position of the player is
 * not changed yet.
 *
 * @server sent to all players that can observe another player (or themselves) starting to move
 * @client send this message to express an intent to move
 */
public class Moving implements Message {
    /**
     * direction of movement
     *
     * @both "LEFT"
     */
    private Direction direction;

    /**
     * local room id of moving player
     *
     * @server 5
     */
    private Integer id;

    public Moving() {
    }

    public Moving(Integer id, Direction direction) {
        this.id = id;
        this.direction = direction;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}

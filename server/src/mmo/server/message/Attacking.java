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
 * Denotes a player or mob starting an attack.
 * <p>
 * Attacks always target (a unit occupying) an adjacent field.
 * <p>
 * Note that the player does still need to finish the attack. This is just an
 * indication that the attack was started. No damage is dealt so far.
 *
 * @server sent when a player or mob starts to attack
 * @client end this message to express an intent to attack
 */
public class Attacking implements Message {
    /**
     * direction of attack
     *
     * @both "LEFT"
     */
    private Direction direction;

    /**
     * local room id of attacking player/mob
     *
     * @server 5
     */
    private Integer id;

    public Attacking(Integer id, Direction direction) {
        this.id = id;
        this.direction = direction;
    }

    public Attacking() {
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

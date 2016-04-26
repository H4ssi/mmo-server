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
 * Denotes failed movement of a player.
 * <p>
 * Note that the movement is considered to be completed, but the position of the
 * player is still unchanged.
 *
 * @server sent when a player failed to successfully execute his/her previously started move (e.g. due to an obstacle)
 */
public class Bump implements Message {
    /**
     * local room id of moved player
     *
     * @server 5
     */
    private int id;

    public Bump() {
    }

    public Bump(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

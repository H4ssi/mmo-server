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
 * Denotes a failed attack.
 * <p>
 * Note that the attack is considered to be completed, but failed to deal any damage to the target.
 *
 * @server sent when a player/mob failed to successfully execute his/her/its previously started attack.
 */
public class Miss implements Message {
    /**
     * local room id of attacking player/mob
     *
     * @server 5
     */
    private int id;

    public Miss(int id) {
        this.id = id;
    }

    public Miss() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

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
 * Denotes a successfully executed attack.
 * <p>
 * Note that the attack is considered to be completed now.
 *
 * @server sent when a player/mob attacked successfully.
 */
public class Hit implements Message {
    /**
     * attacking player/mob
     *
     * @server 5
     */
    private int id;

    /**
     * damage dealt
     * @server 42
     */
    private int damage;

    public Hit(int id, int damage) {
        this.id = id;
        this.damage = damage;
    }

    public Hit() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}

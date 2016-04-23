/*
 * Copyright 2016 Florian Hassanen
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

package mmo.server.data;

import mmo.server.model.Coord;

import java.util.Collection;

public class RoomInfo implements Data {
    private int id;
    private Collection<Coord> obstacles;

    public RoomInfo(int id, Collection<Coord> obstacles) {
        this.id = id;
        this.obstacles = obstacles;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Collection<Coord> getObstacles() {
        return obstacles;
    }

    public void setObstacles(Collection<Coord> obstacles) {
        this.obstacles = obstacles;
    }
}

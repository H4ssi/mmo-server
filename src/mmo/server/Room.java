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

package mmo.server;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import mmo.server.model.Coord;
import mmo.server.model.PlayerInRoom;

import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

public class Room {
    public final static int SIZE = 16;

    private final BiMap<Integer, GameLoop.Callback> ids = HashBiMap.create();
    private final BiMap<Coord, GameLoop.Callback> contents = HashBiMap.create();
    private final BitSet usedIds = new BitSet(SIZE * SIZE);

    public Room() {
    }

    public PlayerInRoom enter(Coord preferred, GameLoop.Callback what) {
        for (int range = 0; range < SIZE; ++range) {
            for (int xoff = -range; xoff <= range; ++xoff) {
                for (int yoff = -range; yoff <= range; ++yoff) {
                    // do not check inner fields
                    if (xoff != -range && xoff != range && yoff != -range &&
                            yoff != range) {
                        continue;
                    }

                    Coord candidate = new Coord(preferred.getX() + xoff,
                            preferred.getY() + yoff);

                    if (!isCoordInRoom(candidate)) {
                        continue;
                    }

                    if (!contents.containsKey(candidate)) {
                        contents.put(candidate, what);
                        int id = nextId();
                        ids.put(id, what);
                        return new PlayerInRoom(id, candidate);
                    }
                }
            }
        }
        return null;
    }

    private int nextId() {
        int id = usedIds.nextClearBit(0);
        usedIds.set(id);
        return id;
    }

    private boolean isCoordInRoom(Coord candidate) {
        return candidate.getX() >= 0 && candidate.getX() < SIZE && candidate
                .getY() >= 0 && candidate.getY() < SIZE;
    }

    public Set<GameLoop.Callback> contents() {
        return Collections.unmodifiableSet(contents.values());
    }

    public int leave(GameLoop.Callback cb) {
        int id = ids.inverse().remove(cb);
        usedIds.clear(id);
        contents.inverse().remove(cb);
        return id;
    }

    public int getId(GameLoop.Callback c) {
        return ids.inverse().get(c);
    }

    public Coord getCoord(GameLoop.Callback c) {
        return contents.inverse().get(c);
    }
}

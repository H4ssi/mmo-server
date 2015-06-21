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
import com.google.common.collect.ImmutableSet;
import mmo.server.model.Coord;
import mmo.server.model.Direction;
import mmo.server.model.Mob;
import mmo.server.model.Player;
import mmo.server.model.PlayerInRoom;
import mmo.server.model.SpawnPoint;

import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

public class Room {
    public final static int SIZE = 16;

    private final BiMap<Integer, Player> ids = HashBiMap.create();
    private final BiMap<Coord, Player> contents = HashBiMap.create();
    private final BitSet usedIds = new BitSet(SIZE * SIZE);
    private final Set<Coord> obstacles;
    private final Set<SpawnPoint> spawnPoints;

    public Room(Set<Coord> obstacles, Set<SpawnPoint> spawnPoints) {
        this.obstacles = ImmutableSet.copyOf(obstacles);
        this.spawnPoints = ImmutableSet.copyOf(spawnPoints);

        for (SpawnPoint p : spawnPoints) {
            spawnMob(p);
        }
    }

    private void spawnMob(SpawnPoint p) {
        SpawnedMob mob = new SpawnedMob("mob", p);

        Coord coord = findFreeNear(p.getCoord());

        if (coord != null) {
            int id = nextId();
            ids.put(id, mob);
            contents.put(coord, mob);
        }
    }

    public Coord findFreeNear(Coord preferred) {
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

                    if (obstacles.contains(candidate)) {
                        continue;
                    }

                    if (!contents.containsKey(candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    public PlayerInRoom enter(Coord coord, Player who) {
        if (contents.containsKey(coord)) {
            return null;
        }

        contents.put(coord, who);
        int id = nextId();
        ids.put(id, who);
        return new PlayerInRoom(id, who, coord);
    }

    private int nextId() {
        int id = usedIds.nextClearBit(0);
        usedIds.set(id);
        return id;
    }

    public boolean isCoordInRoom(Coord candidate) {
        return candidate.getX() >= 0 && candidate.getX() < SIZE && candidate
                .getY() >= 0 && candidate.getY() < SIZE;
    }

    public Set<Player> contents() {
        return Collections.unmodifiableSet(contents.values());
    }

    public int leave(Player player) {
        int id = ids.inverse().remove(player);
        usedIds.clear(id);
        contents.inverse().remove(player);
        return id;
    }

    public int getId(Player player) {
        return ids.inverse().get(player);
    }

    public Coord getCoord(Player player) {
        return contents.inverse().get(player);
    }

    public Player playerAt(Coord coord) {
        return contents.get(coord);
    }

    public boolean movePlayer(Player player, Direction dir) {
        Coord current = getCoord(player);
        Coord target = current.toThe(dir);

        if (obstacles.contains(target)) {
            return false;
        }

        Player other = playerAt(target);

        if (other == null) {
            contents.forcePut(target, player);
            return true;
        } else {
            contents.forcePut(target, player);
            contents.forcePut(current, other);
            return true;
        }
    }

    public Set<Coord> getObstacles() {
        return obstacles;
    }

    private static class SpawnedMob extends Mob {
        private final SpawnPoint point;

        private SpawnedMob(String name, SpawnPoint point) {
            super(name);
            this.point = point;
        }
    }
}

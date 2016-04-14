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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Sets;
import com.google.common.math.IntMath;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;
import mmo.server.message.Attacking;
import mmo.server.message.Bump;
import mmo.server.message.CannotEnter;
import mmo.server.message.Chat;
import mmo.server.message.Entered;
import mmo.server.message.Hit;
import mmo.server.message.InRoom;
import mmo.server.message.Left;
import mmo.server.message.Miss;
import mmo.server.message.Moved;
import mmo.server.message.Moving;
import mmo.server.message.Pwnd;
import mmo.server.message.Spawned;
import mmo.server.model.Coord;
import mmo.server.model.Direction;
import mmo.server.model.Mob;
import mmo.server.model.Player;
import mmo.server.model.PlayerInRoom;
import mmo.server.model.SpawnPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class GameLoop {
    private static final Logger L = LoggerFactory.getLogger(GameLoop.class);

    public static final int ACTION_DELAY_MILLIS = 66;
    private final DefaultEventExecutorGroup loop =
            new DefaultEventExecutorGroup(1);
    private final MessageHub messageHub;

    private BiMap<Room, Integer> roomIds;
    private BiMap<Room, Coord> roomCoords;

    private final HashedWheelTimer timer;

    private static class PlayerState {
        private Integer currentRoom = null;
        private Action action = null;
        private Action queuedAction = null;
        private Direction queuedDirection = null;
    }

    private ConcurrentMap<Player, PlayerState> players =
            new ConcurrentHashMap<>();

    @Inject
    public GameLoop(MessageHub messageHub, HashedWheelTimer timer) {
        this.messageHub = messageHub;
        this.timer = timer;

        BiMap<Room, Integer> roomIds = HashBiMap.create();
        BiMap<Room, Coord> roomCoords = HashBiMap.create();

        Random r = new Random();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                int id = 0;
                if (!(x == 0 && y == 0)) {
                    do {
                        id = r.nextInt();
                    } while (roomIds.containsValue(id));
                }

                Set<Coord> obstacles = new HashSet<>();
                int a, b;
                a = r.nextInt(Room.SIZE / 2);
                b = r.nextInt(Room.SIZE / 2);

                int xbl = Room.SIZE / 4 + Math.min(a, b);
                int xbu = Room.SIZE / 4 + Math.max(a, b);

                a = r.nextInt(Room.SIZE / 2);
                b = r.nextInt(Room.SIZE / 2);

                int ybl = Room.SIZE / 4 + Math.min(a, b);
                int ybu = Room.SIZE / 4 + Math.max(a, b);

                for (int xx = 0; xx < Room.SIZE; ++xx) {
                    for (int yy = 0; yy < Room.SIZE; ++yy) {
                        if (x == -1 && xx == 0
                                || x == 1 && xx == Room.SIZE - 1
                                || y == -1 && yy == 0
                                || y == 1 && yy == Room.SIZE - 1
                                || (!(x == 0 && y == 0)
                                && xx >= xbl && xx <= xbu
                                && yy >= ybl && yy <= ybu)) {
                            obstacles.add(new Coord(xx, yy));
                        }
                    }
                }

                int numMobs = Math.abs(x) + Math.abs(y) + 1;
                Set<SpawnPoint> spawnPoints = new HashSet<>();
                Set<Coord> spawnCoords = new HashSet<>();

                while (spawnPoints.size() < numMobs) {
                    int mx = r.nextInt(Room.SIZE);
                    int my = r.nextInt(Room.SIZE);

                    Coord mspawn = new Coord(mx, my);

                    if (obstacles.contains(mspawn)) {
                        continue;
                    }
                    if (spawnCoords.contains(mspawn)) {
                        continue;
                    }

                    spawnPoints.add(
                            new SpawnPoint(mspawn, r.nextInt(5000) + 5000));
                    spawnCoords.add(mspawn);
                }

                Room room = new Room(obstacles, spawnPoints);
                roomIds.put(room, id);
                roomCoords.put(room, new Coord(x, y));
            }
        }

        this.roomIds = ImmutableBiMap.copyOf(roomIds);
        this.roomCoords = ImmutableBiMap.copyOf(roomCoords);
    }

    public void login(final Player entering) {
        loop.submit(() -> {
            L.debug("login {}", entering);
            final Room room = roomIds.inverse().get(0);

            Coord coord = room.findFreeNear(new Coord(8, 8));

            if (coord == null) {
                messageHub.sendMessage(entering, new CannotEnter());
            } else {
                PlayerState s = new PlayerState();
                players.put(entering, s);
                enterRoom(s, entering, room, coord);
            }
        });
    }

    private void enterRoom(PlayerState state, final Player entering,
                           final Room room, Coord coord) {
        PlayerInRoom enteringPlayerInRoom =
                room.enter(coord, entering);

        int roomId = roomIds.get(room);
        state.currentRoom = roomId;

        messageHub.sendMessage(
                room.contents(),
                new Entered(enteringPlayerInRoom));

        Function<Player, PlayerInRoom> transformToInRoom =
                new Function<Player, PlayerInRoom>() {
                    @Override
                    public PlayerInRoom apply(Player input) {
                        return new PlayerInRoom(room.getId(input),
                                input,
                                room.getCoord(input));
                    }
                };

        Set<Player> others = Sets.filter(
                room.contents(),
                new Predicate<Player>() {
                    @Override
                    public boolean apply(Player input) {
                        return input != entering && !(input instanceof Mob);
                    }
                });
        Collection<PlayerInRoom> othersInRoom = Collections2.transform(
                others, transformToInRoom);

        Collection<Player> mobs = Sets.filter(
                room.contents(),
                Predicates.instanceOf(Mob.class));
        Collection<PlayerInRoom> mobsInRoom = Collections2.transform(
                mobs, transformToInRoom);

        messageHub.sendMessage(
                entering,
                new InRoom(roomId, othersInRoom, mobsInRoom));
    }

    public void logout(final Player leaving) {
        loop.submit(new Runnable() {
            @Override
            public void run() {
                PlayerState s = players.remove(leaving);
                Room room = roomIds.inverse().get(s.currentRoom);

                leaveRoom(s, leaving, room);
            }
        });
    }

    private void leaveRoom(PlayerState state, Player leaving, Room room) {
        int id = room.getId(leaving);
        messageHub.sendMessage(room.contents(), new Left(id));
        room.leave(leaving);
        state.currentRoom = null;
    }

    public void chat(final Player author, final String message) {
        loop.submit(new Runnable() {
            @Override
            public void run() {
                PlayerState s = players.get(author);
                Room room = roomIds.inverse().get(s.currentRoom);

                messageHub.sendMessage(
                        room.contents(),
                        new Chat(room.getId(author), message));
            }
        });
    }

    public Future<?> shutdownGracefully() {
        return loop.shutdownGracefully();
    }

    public void moving(final Player player, final Direction dir) {
        loop.submit(new Runnable() {
            @Override
            public void run() {
                PlayerState s = players.get(player);
                Room room = roomIds.inverse().get(s.currentRoom);
                if (s.action == null) {
                    movingNow(room, player, dir);
                    s.action = Action.MOVE;
                } else {
                    s.queuedAction = Action.MOVE;
                    s.queuedDirection = dir;
                }
            }
        });
    }

    private void movingNow(final Room room,
                           final Player player,
                           final Direction dir) {
        messageHub.sendMessage(
                room.contents(),
                new Moving(room.getId(player), dir)
        );
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                move(player, dir);
            }
        }, ACTION_DELAY_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void move(final Player player, final Direction dir) {
        loop.submit(new Runnable() {
            @Override
            public void run() {
                PlayerState s = players.get(player);
                Room room = roomIds.inverse().get(s.currentRoom);

                Coord target = room.getCoord(player).toThe(dir);
                if (!room.isCoordInRoom(target)) { // will leave room
                    Coord n = new Coord(
                            IntMath.mod(target.getX(), Room.SIZE),
                            IntMath.mod(target.getY(), Room.SIZE));

                    Room next = roomCoords.inverse().get(
                            roomCoords.get(room).toThe(dir));

                    n = next.findFreeNear(n);
                    if (n == null) { // but next room is full
                        messageHub.sendMessage(
                                room.contents(),
                                new Bump(room.getId(player)));
                    } else { // or else just switch rooms
                        leaveRoom(s, player, room);
                        enterRoom(s, player, next, n);
                    }
                } else if (room.movePlayer(player, dir)) { // normal move
                    messageHub.sendMessage(
                            room.contents(),
                            new Moved(room.getId(player))
                    );
                } else { // may fail as well
                    messageHub.sendMessage(
                            room.contents(),
                            new Bump(room.getId(player)));
                }

                workQueue(s, room, player);
            }
        });
    }

    private void workQueue(PlayerState s, Room room, Player player) {
        if (s.queuedAction == null) {
            s.action = null;
        } else {
            switch (s.queuedAction) {
                case MOVE:
                    movingNow(room, player, s.queuedDirection);
                    break;
                case ATTACK:
                    attackingNow(room, player, s.queuedDirection);
                    break;
                default:
                    throw new IllegalStateException("unknown action in queue");
            }
            s.action = s.queuedAction;
            s.queuedAction = null;
            s.queuedDirection = null;
        }
    }

    public void attacking(final Player player, final Direction dir) {
        loop.submit(new Runnable() {
            @Override
            public void run() {
                PlayerState s = players.get(player);
                Room room = roomIds.inverse().get(s.currentRoom);
                if (s.action == null) {
                    attackingNow(room, player, dir);
                    s.action = Action.ATTACK;
                } else {
                    s.queuedAction = Action.ATTACK;
                    s.queuedDirection = dir;
                }
            }
        });
    }

    private void attackingNow(final Room room,
                              final Player player,
                              final Direction dir) {
        messageHub.sendMessage(
                room.contents(),
                new Attacking(room.getId(player), dir)
        );
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                attack(player, dir);
            }
        }, ACTION_DELAY_MILLIS * 3 / 2, TimeUnit.MILLISECONDS);
    }

    private void attack(final Player player, final Direction dir) {
        loop.submit(new Runnable() {
            @Override
            public void run() {
                PlayerState s = players.get(player);
                final Room room = roomIds.inverse().get(s.currentRoom);

                Coord target = room.getCoord(player).toThe(dir);
                if (!room.isCoordInRoom(target)) {
                    messageHub.sendMessage(
                            room.contents(),
                            new Miss(room.getId(player)));
                } else {
                    Player p = room.playerAt(target);
                    if (p instanceof Mob) {
                        messageHub.sendMessage(
                                room.contents(),
                                new Hit(room.getId(player), 1));
                        int mobId = room.getId(p);
                        final SpawnPoint spawn = room.pwn((Mob) p);
                        messageHub.sendMessage(
                                room.contents(),
                                new Pwnd(mobId)
                        );
                        timer.newTimeout(
                                new TimerTask() {
                                    @Override
                                    public void run(Timeout timeout)
                                            throws Exception {
                                        loop.submit(new Runnable() {
                                            @Override
                                            public void run() {
                                                Mob m = room.spawnMob(spawn);
                                                // TODO m == null?
                                                Spawned spawned = new Spawned(
                                                        new PlayerInRoom(
                                                                room.getId(m),
                                                                m,
                                                                room.getCoord(m)
                                                        ));
                                                messageHub.sendMessage(
                                                        room.contents(),
                                                        spawned);
                                            }
                                        });
                                    }
                                },
                                spawn.getIntervalMillis(),
                                TimeUnit.MILLISECONDS
                        );
                    } else {
                        messageHub.sendMessage(
                                room.contents(),
                                new Miss(room.getId(player)));
                    }
                }

                workQueue(s, room, player);
            }
        });
    }

    public Room getRoom(int roomId) {
        return roomIds.inverse().get(roomId);
    }

    private enum Action {
        MOVE, ATTACK,
    }

    private static final String[] vocalStarters = new String[]{
            "a", "e", "i", "o", "u", "au", "ai", "eu"};
    private static final String[] vocals = new String[]{
            "aa", "ao", "ee", "ei", "eo", "ie", "ii", "io", "iu", "oo", "uu"};
    private static final String[] starters = new String[]{
            "b", "br", "bl", "ch", "d", "dr", "dl", "f", "fl", "g", "gr", "gl",
            "h", "j", "k", "kl", "kr", "l", "m", "n", "p", "pl", "qu", "r", "s",
            "sh", "sk", "sl", "sm", "sp", "sr", "st", "t", "tl", "v", "vr",
            "w", "wr", "x", "z", "zr"};
    private static final String[] fillers = new String[]{"bb", "bs", "ck",
            "ds", "dt", "ff", "fs", "ft", "gg", "gs", "gt", "ks", "kt", "ll",
            "lk", "lr", "ls", "lt", "mm", "mp", "mb", "ms", "mt", "nn", "ns",
            "nt", "pp", "rk", "rr", "ss", "tt", "wt", "zz"};

    private static String r(String[] rs) {
        return rs[(int) (Math.random() * rs.length)];
    }

    private static String r(String[] rs1, String[] rs2) {
        int s = (int) (Math.random() * (rs1.length + rs2.length));

        if (s < rs1.length) {
            return rs1[s];
        } else {
            return rs2[s % rs1.length];
        }
    }

    public static String generateName() {
        int vs = (int) (Math.random() * 2) + 1;

        StringBuilder b = new StringBuilder();

        if (Math.random() > 0.5) {
            b.append(r(vocalStarters));
            if (vs == 1) {
                b.append(r(starters, fillers));
            }
        } else {
            b.append(r(starters));
            b.append(r(vocalStarters, vocals));
        }

        for (int v = 1; v < vs; v++) {
            b.append(r(starters, fillers));
            b.append(r(vocalStarters, vocals));
        }

        if (Math.random() > 0.5) {
            b.append(r(starters, fillers));
        }

        return b.toString();
    }
}

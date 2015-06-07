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
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.math.IntMath;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;
import mmo.server.message.Bump;
import mmo.server.message.CannotEnter;
import mmo.server.message.Chat;
import mmo.server.message.Entered;
import mmo.server.message.InRoom;
import mmo.server.message.Left;
import mmo.server.message.Moved;
import mmo.server.message.Moving;
import mmo.server.model.Coord;
import mmo.server.model.Direction;
import mmo.server.model.Player;
import mmo.server.model.PlayerInRoom;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class GameLoop {
    private final DefaultEventExecutorGroup loop =
            new DefaultEventExecutorGroup(1);
    private final MessageHub messageHub;

    private BiMap<Room, Integer> roomIds = HashBiMap.create();
    private BiMap<Room, Coord> roomCoords = HashBiMap.create();

    private final HashedWheelTimer timer;

    private static class PlayerState {
        private Integer currentRoom = null;
        private boolean isMoving = false;
        private Direction queuedMoving = null;
    }

    private ConcurrentMap<Player, PlayerState> players =
            new ConcurrentHashMap<>();

    @Inject
    public GameLoop(MessageHub messageHub, HashedWheelTimer timer) {
        this.messageHub = messageHub;
        this.timer = timer;

        Random r = new Random();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                int id = 0;
                if (!(x == 0 && y == 0)) {
                    do {
                        id = r.nextInt();
                    } while (roomIds.containsValue(id));
                }

                Room room = new Room();
                roomIds.put(room, id);
                roomCoords.put(room, new Coord(x, y));
            }
        }
    }

    public void login(final Player entering) {
        loop.submit(new Runnable() {
            @Override
            public void run() {
                final Room room = roomIds.inverse().get(0);

                Coord coord = room.findFreeNear(new Coord(8, 8));

                if (coord == null) {
                    messageHub.sendMessage(entering, new CannotEnter());
                } else {
                    PlayerState s = new PlayerState();
                    players.put(entering, s);
                    enterRoom(s, entering, room, coord);
                }
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

        Iterable<Player> others = Iterables.filter(
                room.contents(),
                new Predicate<Player>() {
                    @Override
                    public boolean apply(Player input) {
                        return input != entering;
                    }
                }
        );
        Iterable<PlayerInRoom> othresInRoom = Iterables.transform(
                others, new Function<Player, PlayerInRoom>() {
                    @Override
                    public PlayerInRoom apply(Player input) {
                        return new PlayerInRoom(room
                                .getId(input),
                                input,
                                room.getCoord(input));
                    }
                }
        );
        messageHub.sendMessage(
                entering,
                new InRoom(roomId, Iterables.toArray(
                        othresInRoom, PlayerInRoom.class)));
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
                if (!s.isMoving) {
                    movingNow(room, player, dir);
                    s.isMoving = true;
                } else {
                    s.queuedMoving = dir;
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
        }, 66, TimeUnit.MILLISECONDS);
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

                if (s.queuedMoving == null) {
                    s.isMoving = false;
                } else {
                    movingNow(room, player, s.queuedMoving);
                    s.queuedMoving = null;
                }
            }
        });
    }
}

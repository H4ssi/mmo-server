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

import com.google.common.collect.Sets;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;
import mmo.server.model.Coord;
import mmo.server.model.PlayerInRoom;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Singleton
public class GameLoop {
    private final DefaultEventExecutorGroup loop;
    private Room room = new Room();

    public interface Callback {
        void tick();

        void tock();

        void cannotEnter();

        void entered(int id, Coord coord);

        void left(int id);

        void inRoom(List<PlayerInRoom> inRoom);

        void chat(String message);
    }

    private final HashedWheelTimer timer;
    private boolean tickTock = false;
    private TimerTask task = new TimerTask() {

        @Override
        public void run(Timeout timeout) throws Exception {
            tickTock ^= true;
            if (tickTock) {
                tick();
            } else {
                tock();
            }
            schedule();
        }
    };
    private Set<Callback> callbacks = Sets.newConcurrentHashSet();

    @Inject
    public GameLoop(HashedWheelTimer timer) {
        this.timer = timer;
        schedule();
        loop = new DefaultEventExecutorGroup(1);
    }

    private void tock() {
        System.out.println("tock");
        for (Callback cb : callbacks) {
            cb.tock();
        }
    }

    private void tick() {
        System.out.println("tick");
        for (Callback cb : callbacks) {
            cb.tick();
        }
    }

    private void schedule() {
        timer.newTimeout(task, 1, TimeUnit.SECONDS);
    }

    public void login(final Callback cb) {
        loop.submit(new Runnable() {
            @Override
            public void run() {
                callbacks.add(cb);

                Coord coord = room.enter(new Coord(8, 8), cb);
                if (coord == null) {
                    cb.cannotEnter();
                } else {
                    List<PlayerInRoom> data = new LinkedList<PlayerInRoom>();
                    for (Callback c : room.contents()) {
                        c.entered(room.getId(cb), coord);
                        if (c != cb) {
                            data.add(new PlayerInRoom(
                                    room.getId(c),
                                    room.getCoord(c)
                            ));
                        }
                    }

                    cb.inRoom(data);
                }
            }
        });
    }

    public void logout(final Callback cb) {
        loop.submit(new Runnable() {
            @Override
            public void run() {
                int id = room.leave(cb);
                for (Callback c : room.contents()) {
                    c.left(id);
                }

                callbacks.remove(cb);
            }
        });
    }

    public void chat(final String message) {
        loop.submit(new Runnable() {
            @Override
            public void run() {
                for (Callback c : room.contents()) {
                    c.chat(message);
                }
            }
        });
    }

    public Future<?> shutdownGracefully() {
        return loop.shutdownGracefully();
    }
}

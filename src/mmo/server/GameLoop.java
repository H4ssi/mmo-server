package mmo.server;

import com.google.common.collect.Sets;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;
import mmo.server.model.Coord;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
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

        void endered(Coord coord);

        void left(Coord coord);

        void inRoom(Map<Coord, Callback> inRoom);
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

    public void login(Callback cb) {
        callbacks.add(cb);

        Coord coord = room.enter(new Coord(8, 8), cb);
        if (coord == null) {
            cb.cannotEnter();
        } else {
            for (Callback c : room.contents().values()) {
                c.endered(coord);
            }
            cb.inRoom(room.contents());
        }
    }

    public void logout(Callback cb) {
        Coord coord = room.leave(cb);
        for (Callback c : room.contents().values()) {
            c.left(coord);
        }

        callbacks.remove(cb);
    }

    public Future<?> shutdownGracefully() {
        return loop.shutdownGracefully();
    }
}

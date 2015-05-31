package mmo.server;

import com.google.common.collect.Sets;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Singleton
public class Clock {
    public interface Callback {
        void tick();

        void tock();
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
    public Clock(HashedWheelTimer timer) {
        this.timer = timer;
        schedule();
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

    public void addCallback(Callback cb) {
        callbacks.add(cb);
    }

    public void removeCallback(Callback cb) {
        callbacks.remove(cb);
    }
}

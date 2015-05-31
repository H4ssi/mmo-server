package mmo.server;

import dagger.Module;
import dagger.Provides;
import io.netty.util.HashedWheelTimer;

import javax.inject.Singleton;

@Module(injects = Server.class)
public class ServerModule {

    @Provides
    @Singleton
    HashedWheelTimer provideHashedWheelTimer() {
        return new HashedWheelTimer();
    }
}

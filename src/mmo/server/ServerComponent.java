package mmo.server;

import dagger.Component;

import javax.inject.Singleton;

/**
 * Created by flori on 14.04.2016.
 */
@Singleton
@Component(
        modules = {
                ServerModule.class
        }
)
public interface ServerComponent {
    Server getServer();
}

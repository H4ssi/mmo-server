package mmo.server;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    @Provides
    @Singleton
    ObjectMapper provideObjectMapper() {
        return new ObjectMapper()
                .setDefaultTyping(
                        new ObjectMapper.DefaultTypeResolverBuilder(
                                ObjectMapper.DefaultTyping
                                        .OBJECT_AND_NON_CONCRETE)
                                .init(
                                        JsonTypeInfo.Id.MINIMAL_CLASS,
                                        null)
                                .inclusion(JsonTypeInfo.As.PROPERTY)
                                .typeProperty("type")
                )
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
}

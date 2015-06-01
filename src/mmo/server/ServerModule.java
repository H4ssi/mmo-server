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

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

import dagger.ObjectGraph;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger L = Logger.getAnonymousLogger();

    public static void main(String[] args) throws IOException,
            InterruptedException {

        Logger.getGlobal().setLevel(Level.ALL);

        // openshift host+port
        String host = System.getenv("OPENSHIFT_DIY_IP");
        if (host == null) {
            host = "0.0.0.0";
        }
        String portStr = System.getenv("OPENSHIFT_DIY_PORT");
        int port = 8080;
        if (portStr != null) {
            port = Integer.parseInt(portStr);
        }

        ObjectGraph objectGraph = ObjectGraph.create(new ServerModule());
        Server server = objectGraph.get(Server.class);
        server.run(host, port);
        L.info("Server started (press enter to shutdown)");
        System.in.read();
        L.info("Server shutting down...");
        server.shutdown();
    }
}

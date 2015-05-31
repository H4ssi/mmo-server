package mmo.server;

import dagger.ObjectGraph;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException,
            InterruptedException {
        ObjectGraph objectGraph = ObjectGraph.create(new ServerModule());
        Server server = objectGraph.get(Server.class);
        server.run();
        System.out.println("Server started (press enter to shutdown)");
        System.in.read();
        System.out.println("Server shutting down...");
        server.shutdown();
    }
}

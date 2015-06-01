package mmo.server;

import dagger.ObjectGraph;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException,
            InterruptedException {

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
        System.out.println("Server started (press enter to shutdown)");
        System.in.read();
        System.out.println("Server shutting down...");
        server.shutdown();
    }
}

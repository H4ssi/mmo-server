package mmo.server;

import dagger.ObjectGraph;

public class Main {
	public static void main(String[] args) {
		ObjectGraph objectGraph = ObjectGraph.create(new ServerModule());
		Server server = objectGraph.get(Server.class);
		server.run();
	}
}

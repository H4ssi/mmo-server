package mmo.server.message;

import mmo.server.model.Coord;

public class InRoom implements Message {
    private Coord[] coords;

    public InRoom(Coord[] coords) {
        this.coords = coords;
    }

    public Coord[] getCoords() {
        return coords;
    }

    public void setCoords(Coord[] coords) {
        this.coords = coords;
    }
}

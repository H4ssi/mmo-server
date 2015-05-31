package mmo.server.message;

import mmo.server.model.Coord;

/**
 * Created by florian on 5/31/15.
 */
public class Entered extends Coord implements Message {
    public Entered(int x, int y) {
        super(x, y);
    }
}

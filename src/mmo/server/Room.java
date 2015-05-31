package mmo.server;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import mmo.server.model.Coord;

import java.util.Collections;
import java.util.Map;

/**
 * Created by florian on 5/31/15.
 */
public class Room {
    private final static int SIZE = 16;

    private final BiMap<Coord, GameLoop.Callback> contents = HashBiMap.create();

    public Coord enter(Coord preferred, GameLoop.Callback what) {
        for (int range = 0; range < SIZE; ++range) {
            for (int xoff = -range; xoff <= range; ++xoff) {
                for (int yoff = -range; yoff <= range; ++yoff) {
                    // do not check inner fields
                    if (xoff != -range && xoff != range && yoff != -range &&
                            yoff != range) {
                        continue;
                    }

                    Coord candidate = new Coord(preferred.getX() + xoff,
                            preferred.getY() + yoff);

                    if (!inRoom(candidate)) {
                        continue;
                    }

                    if (!contents.containsKey(candidate)) {
                        contents.put(candidate, what);
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private boolean inRoom(Coord candidate) {
        return candidate.getX() >= 0 && candidate.getX() < SIZE && candidate
                .getY() >= 0 && candidate.getY() < SIZE;
    }

    public Map<Coord, GameLoop.Callback> contents() {
        return Collections.unmodifiableMap(contents);
    }

    public Coord leave(GameLoop.Callback cb) {
        return contents.inverse().remove(cb);
    }
}

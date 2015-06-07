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

import mmo.server.model.Coord;
import mmo.server.model.Player;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RoomTest {
    private static Player createDummyPlayer() {
        return new Player("dummy");
    }

    @Test
    public void enterEmptyRoom() {
        Room room = new Room(Collections.<Coord>emptySet());

        Player pl = createDummyPlayer();
        assertThat("empty room can be entered",
                room.enter(new Coord(1, 2), pl),
                allOf(
                        notNullValue(),
                        hasProperty("id", is(0)),
                        hasProperty("coord", is(new Coord(1, 2)))));

        assertThat("player is in room",
                room.contents(), hasItem(pl));
    }

    @Test
    public void enterRoomAndGetDisplaced() {
        Room room = new Room(Collections.<Coord>emptySet());

        room.enter(new Coord(0, 0), createDummyPlayer());

        assertThat("proper displacment can be found if tile is occupied",
                room.findFreeNear(new Coord(0, 0)),
                not(is(new Coord(0, 0))));

        assertThat("player cannot enter on same spot",
                room.enter(new Coord(0, 0), createDummyPlayer()),
                nullValue());
    }

    @Test
    public void useConsecutiveIds() {
        Room room = new Room(Collections.<Coord>emptySet());

        assertThat("first player gets id 0",
                room.enter(new Coord(0, 0), createDummyPlayer()).getId(),
                is(0));
        Player pl1 = createDummyPlayer();
        assertThat("second player gets id 1",
                room.enter(new Coord(0, 1), pl1).getId(),
                is(1));
        Player pl2 = createDummyPlayer();
        assertThat("third player gets id 2",
                room.enter(new Coord(0, 2), pl2).getId(),
                is(2));
        assertThat("first player gets id 0",
                room.enter(new Coord(0, 3), createDummyPlayer()).getId(),
                is(3));
        assertThat("id 1 leaves room", room.leave(pl1), is(1));
        assertThat("id 2 leaves room", room.leave(pl2), is(2));
        assertThat("next player gets id 1 again",
                room.enter(new Coord(0, 4), createDummyPlayer()).getId(),
                is(1));
    }

    @Test
    public void enterFullRoom() {
        Room room = new Room(Collections.<Coord>emptySet());

        for (int i = 0; i < Room.SIZE * Room.SIZE; ++i) {
            room.enter(new Coord(0, 0), createDummyPlayer());
        }

        assertThat("room cannot be entered if it is already full",
                room.enter(new Coord(0, 0), createDummyPlayer()),
                nullValue());
    }
}

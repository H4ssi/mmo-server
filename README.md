## Building and running

Use `./gradlew compile`, `./gradlew run` to build resp. run the server.
Most likely it will not work, if you try to run it from the IDE out of the box.

When run, the server will listen on port 8080 for HTTP connections.

## Game logic and mechanics

mmo-server simulates a virtual world with avatars for players as well as non 
player characters. Players venture through this virtual world for honor, 
fame, glory and the like. But mostly for fun!

### Rooms

The world is sectioned into "rooms". Every room has a static layout, squarely
tiled. A room measures 16 tiles in length and 16 tiles in width. An entity 
can only be placed on such a tile (in between positions are disregarded).

A Client can only observe and interact within the bounds of the current room.
Thus a client has to hop from room to room to gradually observe and interact 
with the whole world.

## Protocol

mmo-server communicates over HTTP. You are welcome to use your browser to 
observe the data sent from the server. For a quick demonstration, just point 
your browser to [http://localhost:8080/game](http://localhost:8080/game). 
(Firefox is recommended) - provided you started the server on your local 
machine.

### Index page

```
GET /
```

The index page contains general information on the server: 
[http://localhost:8080/](http://localhost:8080/)

The data is free form text.

### Obtain server status

```
GET /status
```

Gets current information on the server's status: 
[http://localhost:8080/status](http://localhost:8080/status)

The data is JSON formatted, see this sample:

```
{
  "status" : "up",
  "messageOfTheDay" : "Chuck Norris only needs one (1) pokeball to catch legendery pokemon."
}
```

### Notification channel

```
GET /game           /* login as "anonymous" */
GET /game/your-name /* login as "your-name" */
```

The notification channel is used to send and receive events to and from the 
server. 

HTTP push is used for this purpose (in both directions: from the server to 
the client and vice versa).

Take a look at the communication with your browser: 
[http://localhost:8080/game](http://localhost:8080/game)

You may choose between logging in anonymously or with a name of your choice. 
Names are _not_ unique, i.e. multiple users may very well use the very same 
name.

#### Channel initialization

The very first message sent from the server upon connection reads

```
<!DOCTYPE html><html><body><pre>
```

This message solely aids the purpose of nice rendering in major browsers and
thus _should be ignored on any other client_.

#### Message exchange

The exchanged messages are JSON encoded.

There are several kinds of messages supported by the server. Each message is 
tagged with a type hint, such that its kind can be derived:

```
{
    "type" : ".<TypeName>"
    ...
}
```

E.g.

```
{
  "type" : ".Entered",
  "x" : 7,
  "y" : 8,
  "id" : 1
  "name" : "florian"
}
```

_Do note the preceding `.` (dot)!_

#### Chatting (`Chat`)

Server or client sends

```
{
  "type" : ".Chat",
  "id" : 12                   /* author local room  id : optional[int] */
  "message" : "Hello there!"  /* chat message : string */
}
```

which means

* a client wants to send a chat message to the room (when the message is sent
  from the client to the server)
  * clients shall not include the `id` field when sending messages. The id 
    will be filled out by the server.
* or, a client sends you a message (when the message is sent from the server 
  to the client)
  * if the chat message was sent from a client, the `id` field will be included
  * the `id` field can be ommited, e.g. for global server status broadcasts

Chat messages are supposed to be HTML encoded (Thus clients should handle
rendering and special characters like `<`, `>` etc. correctly). Client
input is sanitized by the server prior to dispatching.

#### Entering a room (`Entered`)

Server sends

```
{
  "type" : ".Entered",
  "x" : 7,           /* x coordinate : int */
  "y" : 8,           /* y coordinate : int */
  "id" : 1           /* room local id of player : int */ 
  "name" : "florian" /* name of entering user : string */
}
```

which means, a player named `"florian"` entered the room and was placed on tile 
`[7/8]`. As long as the player stays in this room, they will be identified by 
the id  `1`.
 
The server will also notify the entering player themselves, in fact it is the
very first message sent to the client upon entering a room.

#### Room's contents (`InRoom`)

Server sends
```
{
  "type" : ".InRoom",
  "coords" : [ {    /* list of players currently in this room : array */
    "id" : 0,       /* player room id : int */
    "x" : 8,        /* player x coordinate : int */
    "y" : 8         /* player y coordinate : int */
    "name" : "bert" /* player name : string */
  }, {
    "id" : 2,
    "x" : 7,
    "y" : 8
    "name" : "ernie"
  } ]
}
```

which means, besides the client, there are two players in this room currently:

* Player `"bert"` with local room id `0` on tile `[8/8]`
* Player `"ernie"` with local room id `2` on tile `[7/8]`

This message is sent to the client as second message (right after `Entered`) 
upon entering a room.

#### Leaving (`Left`)

Server sends

```
{
  "type" : ".Left",
  "id" : 0         /* player room id : int */
}
```

which means, the player with local room id `0` just left the room. Upon 
leaving a room, this is the last message, that is still relevant for this room.

## License

mmo-server is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the 
License, or (at your option) any later version.

mmo-server is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public
License along with mmo-server.  If not, see 
[<http://www.gnu.org/licenses/>](http://www.gnu.org/licenses/).

## Powered by

* [netty](http://netty.io/) Copyright 2014 The Netty Project
* [jackson](http://wiki.fasterxml.com/JacksonHome)
* [dagger](http://square.github.io/dagger/) Copyright 2013 Square, Inc.
* [autoFactory](https://github.com/google/auto/tree/master/factory)
  Copyright 2013 Google, Inc.
* [guava](https://github.com/google/guava)
* [jsoup](http://jsoup.org/) Copyright 2009-2015 Jonathan Hedley
* [testng](http://testng.org/)
* [hamcrest](http://hamcrest.org/JavaHamcrest/) 
  Copyright 2000-2015 www.hamcrest.org

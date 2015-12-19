package com.websocket.chat.registry.Model;

/**
 * Created by Robin on 2015-12-18.
 *
 * Room data stored in the Registry.
 */
public class Room {
    private String room;
    private Integer hit = 0;

    public Room(String room) {
        this.room = room;
    }

    public void hit() {
        if (hit == Integer.MAX_VALUE)
            hit = 0;

        hit++;
    }

    public Integer getHits() {
        return hit;
    }

    public String getName() {
        return room;
    }

    public void setName(String name) {
        this.room = name;
    }
}

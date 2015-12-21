package com.websocket.chat.registry.Protocol;

/**
 * Created by Robin on 2015-12-18.
 *
 * A request for a server.
 */
public class Lookup {
    public static final String ACTION = "registry.lookup";
    private Header header;
    private String room;

    public Lookup() {
        this(null);
    }

    public Lookup(String room) {
        this.room = room;
        this.header = new Header(ACTION);
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}

package com.websocket.chat.registry.Protocol;

/**
 * Created by Robin on 2015-12-18.
 * <p>
 * Received from the connector-backend to indicate
 * which servers have users in which rooms.
 */
public class RoomEvent {
    public static final String ACTION = "registry.room";
    private RoomStatus status;
    private String server;
    private String room;
    private Header header;

    public RoomEvent(String server, String room, RoomStatus status) {
        this();
        this.server = server;
        this.room = room;
        this.status = status;
    }

    public RoomEvent() {
        this.header = new Header(ACTION);
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public enum RoomStatus {
        POPULATED,
        DEPLETED
    }
}

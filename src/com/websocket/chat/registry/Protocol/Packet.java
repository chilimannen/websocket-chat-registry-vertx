package com.websocket.chat.registry.Protocol;

/**
 * Created by Robin on 2015-12-18.
 *
 * Generic container for partial unpacking to inspect the action/message type.
 */
public class Packet {
    private Header header;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getAction() {
        return header.getAction();
    }
}

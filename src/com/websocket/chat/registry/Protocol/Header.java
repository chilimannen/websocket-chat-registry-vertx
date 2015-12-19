package com.websocket.chat.registry.Protocol;

/**
 * Created by Robin on 2015-12-18.
 *
 * Header indicating the type of message.
 */
public class Header {
    private String action;

    public Header() {
    }

    public Header(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}

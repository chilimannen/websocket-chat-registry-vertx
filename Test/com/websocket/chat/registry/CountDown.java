package com.websocket.chat.registry;

/**
 * Created by Robin on 2015-12-19.
 */
public class CountDown {
    private Integer counts;

    public CountDown(Integer counts) {
        this.counts = counts;
    }

    public Integer down() {
        counts -= 1;
        return counts;
    }

    public Integer left() {
        return counts;
    }
}

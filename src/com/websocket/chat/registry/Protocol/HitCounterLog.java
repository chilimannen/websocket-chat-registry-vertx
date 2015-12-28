package com.websocket.chat.registry.Protocol;

import com.websocket.chat.registry.Configuration;

/**
 * Created by Robin on 2015-12-28.
 * <p>
 * Contains the number of hits on the service.
 */
public class HitCounterLog {
    private Integer hits;
    private String name = Configuration.REGISTER_NAME;
    private String type = "logging.hits";

    public HitCounterLog(Integer hits) {
        this.hits = hits;
    }

    public Integer getHits() {
        return hits;
    }

    public void setHits(Integer hits) {
        this.hits = hits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.websocket.chat.registry.Protocol;

import com.websocket.chat.registry.Configuration;

/**
 * Created by Robin on 2015-12-28.
 * <p>
 * Contains the number of hits on the service.
 */
public class IOLogger {
    private Integer in;
    private Integer out;
    private String name = Configuration.REGISTER_NAME;
    private String type = "logging.io";

    public IOLogger() {}

    public IOLogger(Integer in, Integer out) {
        this.in = in;
        this.out = out;
    }

    public Integer getOut() {
        return out;
    }

    public void setOut(Integer out) {
        this.out = out;
    }

    public Integer getIn() {
        return in;
    }

    public void setIn(Integer in) {
        this.in = in;
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

    public void in() {
        in += 1;
    }

    public void reset() {
        this.in = 0;
        this.out = 0;
    }

    public void out() {
        out += 1;
    }
}

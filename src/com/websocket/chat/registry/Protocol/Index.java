package com.websocket.chat.registry.Protocol;

import com.websocket.chat.registry.Model.Server;

/**
 * Created by Robin on 2015-12-18.
 *
 * A response to a Lookup.
 */
public class Index {
    private Header header;
    private String ip;
    private String name;
    private Integer port;
    private Boolean full = false;

    public Index() {
        this(new Server(null, null, null));
    }

    public Index(Server server) {
        header = new Header("lookup.index");
        this.ip = server.getIp();
        this.name = server.getName();
        this.port = server.getPort();
    }

    public Boolean getFull() {
        return full;
    }

    public Index setFull(Boolean full) {
        this.full = full;
        return this;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}

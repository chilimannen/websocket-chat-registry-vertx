package com.websocket.chat.registry.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Robin on 2015-12-18.
 *
 * Server data stored in the registry.
 */
public class Server {
    private String name;
    private String ip;
    private Integer port;
    private Boolean full = false;
    private Map<String, Room> rooms = new HashMap<>();

    public Server(String name, String ip, Integer port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getFull() {
        return full;
    }

    public void setFull(Boolean full) {
        this.full = full;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public void setRooms(Map<String, Room> rooms) {
        this.rooms = rooms;
    }
}

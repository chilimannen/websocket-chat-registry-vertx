package com.websocket.chat.registry.Protocol;

import com.websocket.chat.registry.Configuration;
import com.websocket.chat.registry.Model.Server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Robin on 2015-12-28.
 * <p>
 * Contains a tree of all the registered servers.
 */
public class ServerTreeLog {
    private Collection<Server> servers;
    private String type = "logging.servers";
    private String name = Configuration.REGISTER_NAME;

    public ServerTreeLog(Map<String, Server> servers) {
        this.servers = servers.values();
    }

    public Collection<Server> getServers() {
        return servers;
    }

    public void setServers(ArrayList<Server> servers) {
        this.servers = servers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

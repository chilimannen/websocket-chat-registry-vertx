package com.websocket.chat.registry;

import com.websocket.chat.registry.Exception.NoServersFound;
import com.websocket.chat.registry.Model.Room;
import com.websocket.chat.registry.Protocol.Header;
import com.websocket.chat.registry.Protocol.Packet;
import com.websocket.chat.registry.Protocol.Serializer;
import com.websocket.chat.registry.Model.Server;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

import java.util.*;

/**
 * Created by Robin on 2015-12-18.
 * <p>
 * Warning: the chatservers must be provisioned with enough processing
 * capacity to handle the number of maximum assigned users at MAX load.
 * This is because already connected users are not relocated during heavy load.
 */
public class RegistryService implements Verticle {
    private Map<String, Server> servers = new HashMap<>();
    private Map<String, EventHandler> eventHandler = new HashMap<>();
    private Map<String, MessageHandler> messageHandler = new HashMap<>();
    private Vertx vertx;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        eventHandler.put("registry.room", EventHandler.HandleRoom);
        eventHandler.put("registry.server", EventHandler.HandleServer);

        messageHandler.put("registry.lookup", MessageHandler.HandleLookup);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startRegistryEventListener();
        startRegistryLookupService();
    }

    private void startRegistryEventListener() {
        vertx.createHttpServer().websocketHandler(event -> {
            event.handler(data -> {

                Packet packet = (Packet) Serializer.unpack(data.toString(), Packet.class);

                if (eventHandler.get(packet.getAction()) != null)
                    eventHandler.get(packet.getAction()).handle(data.toString(), this);
            });

        }).listen(Launcher.CONNECTOR_PORT);
    }

    private void startRegistryLookupService() {
        vertx.createHttpServer().websocketHandler(event -> {
            event.handler(data -> {

                System.out.println("lookupService: " + data.toString());

                Packet packet = (Packet) Serializer.unpack(data.toString(), Packet.class);

                if (messageHandler.get(packet.getAction()) != null)
                    messageHandler.get(packet.getAction()).handle(
                            event.textHandlerID(), data.toString(), this);
            });

        }).listen(Launcher.LISTEN_PORT, "localhost");
    }


    protected void sendBus(String address, Object data) {
        vertx.eventBus().send(address, Serializer.pack(data));
    }

    /**
     * Get a list of servers that is advertised as ready for more clients.
     *
     * @return List of all servers ready for clients.
     */
    protected ArrayList<Server> getReadyServers() {
        ArrayList<Server> list = new ArrayList<>();

        for (Server server : servers.values()) {
            if (!server.getFull())
                list.add(server);
        }
        return list;
    }


    /**
     * Returns a single server that is ready for more clients.
     *
     * @param roomName servers subscribed to roomName a preferred
     *                 as it is beneficial to group users to minimize
     *                 replication. Rooms that have more hits/connects are also
     *                 preferred.
     * @return A single server that has the highest match priority, if all servers
     * have equal priority the server will be randomized.
     * @throws NoServersFound if no servers are subscribed on the connector
     *                        or if all subscribed connectors are full.
     */
    protected Server getReadyServer(String roomName) throws NoServersFound {
        ArrayList<Server> ready = new ArrayList<>(getReadyServers());

        try {
            Server preferred = ready.get(new Random().nextInt(ready.size()));
            Integer hits = -1;

            for (Server server : ready) {
                Room room = server.getRooms().get(roomName);

                if (room != null && room.getHits() > hits) {
                    preferred = server;
                    hits = room.getHits();
                }
            }

            return preferred;
        } catch (IllegalArgumentException e) {
            throw new NoServersFound();
        }
    }


    protected void removeRoom(String name, String room) {
        Server server = servers.get(name);

        if (server != null)
            server.getRooms().remove(room);
    }

    protected void addRoom(String name, Room room) {
        Server server = servers.get(name);

        if (server != null)
            server.getRooms().put(room.getName(), room);
    }


    public void setFull(String name, boolean isFull) {
        Server server = servers.get(name);

        if (server != null)
            server.setFull(isFull);
    }

    protected void removeServer(String server) {
        servers.remove(server);
    }

    protected void addServer(Server server) {
        servers.put(server.getName(), server);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
    }
}

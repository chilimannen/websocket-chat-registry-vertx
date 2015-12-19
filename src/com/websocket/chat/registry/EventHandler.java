package com.websocket.chat.registry;

import com.websocket.chat.registry.Model.Room;
import com.websocket.chat.registry.Model.Server;
import com.websocket.chat.registry.Protocol.RoomEvent;
import com.websocket.chat.registry.Protocol.Serializer;
import com.websocket.chat.registry.Protocol.ServerEvent;

/**
 * Created by Robin on 2015-12-18.
 * <p>
 * Handles events from the backend regarding changes
 * in the list of servers that are subscribed to a room.
 *
 * Handles events that indicate server state as well.
 */
enum EventHandler {

    HandleRoom {
        @Override
        public void handle(String data, RegistryService registry) {
            RoomEvent room = (RoomEvent) Serializer.unpack(data, RoomEvent.class);

            if (room.getStatus() != null)
                switch (room.getStatus()) {
                    case POPULATED:
                        registry.addRoom(room.getServer(), new Room(room.getRoom()));
                        break;
                    case DEPLETED:
                        registry.removeRoom(room.getServer(), room.getRoom());
                        break;
                }
        }
    },

    HandleServer {
        @Override
        public void handle(String data, RegistryService registry) {
            ServerEvent server = (ServerEvent) Serializer.unpack(data, ServerEvent.class);

            if (server.getStatus() != null)
                switch (server.getStatus()) {
                    case UP:
                        registry.addServer(new Server(server.getName(), server.getIp(), server.getPort()));
                        break;
                    case DOWN:
                        registry.removeServer(server.getName());
                        break;
                    case FULL:
                        registry.setFull(server.getName(), true);
                        break;
                    case READY:
                        registry.setFull(server.getName(), false);
                        break;
                }
        }
    };

    public abstract void handle(String data, RegistryService registry);
}

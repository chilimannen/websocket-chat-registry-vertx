package com.websocket.chat.registry;

import com.websocket.chat.registry.Exception.NoServersFound;
import com.websocket.chat.registry.Model.Server;
import com.websocket.chat.registry.Protocol.Index;
import com.websocket.chat.registry.Protocol.Lookup;
import com.websocket.chat.registry.Protocol.Serializer;

/**
 * Created by Robin on 2015-12-18.
 */
enum MessageHandler {
    HandleLookup {
        @Override
        public void handle(String socket, String data, RegistryService registry) {
            Lookup lookup = (Lookup) Serializer.unpack(data, Lookup.class);
            try {
                Server server = registry.getReadyServer(lookup.getRoom());
                registry.sendBus(socket, new Index(server));
            } catch (NoServersFound e) {
                registry.sendBus(socket, new Index().setFull(true));
            }
        }
    };

    public abstract void handle(String socket, String data, RegistryService registry);
}

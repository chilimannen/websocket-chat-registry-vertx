package com.websocket.chat.registry;

import com.websocket.chat.registry.Exception.NoServersFound;
import com.websocket.chat.registry.Model.Room;
import com.websocket.chat.registry.Model.Server;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Robin on 2015-12-21.
 * <p>
 * Registry unit tests.
 */
@RunWith(VertxUnitRunner.class)
public class RegistryUnit {

    @Rule
    public RepeatRule rule = new RepeatRule();

    @Test
    public void shouldRegisterServer(TestContext context) throws NoServersFound {
        RegistryService service = new RegistryService();
        service.addServer(new Server("name", "localhost", 8080));
        Server server = service.getReadyServer("");
        context.assertEquals("name", server.getName());
        context.assertEquals("localhost", server.getIp());
        context.assertEquals(8080, server.getPort());
        context.assertEquals(false, server.getFull());
    }

    @Test
    public void shouldUnregisterServer() throws Exception {
        RegistryService service = new RegistryService();
        Server server = new Server("", "", 80);
        service.addServer(server);
        service.removeServer(server.getName());

        try {
            service.getReadyServer("any");
            throw new Exception("Should fail with NoServersFound()");
        } catch (NoServersFound ignored) {
        }
    }

    @Test
    public void shouldNotBalanceToFull() throws Exception {
        RegistryService service = new RegistryService();
        Server server = new Server("", "", 80);
        service.addServer(server);
        service.setFull(server.getName(), true);


        try {
            service.getReadyServer("any");
            throw new Exception("Should fail with NoServersFound()");
        } catch (NoServersFound ignored) {
        }
    }

    @Test
    public void shouldReEnableWhenReady() throws NoServersFound {
        RegistryService registry = new RegistryService();
        Server server = new Server("", "", 80);
        registry.addServer(server);
        registry.setFull(server.getName(), true);
        registry.setFull(server.getName(), false);

        registry.getReadyServer("any");
    }

    @Test
    public void shouldPreferServerWithMoreHits() throws Exception {
        RegistryService registry = new RegistryService();
        Server server = new Server("first", "", 80);
        Server preferred = new Server("preferred", "", 80);
        registry.addServer(server);
        registry.addServer(preferred);

        // add and add a hit to the preferred server.
        registry.addRoom(preferred.getName(), new Room("the_room"));
        registry.getReadyServer("the_room");

        registry.addRoom(server.getName(), new Room("the_room"));

        for (int i = 0; i < 1000; i++) {
            if (!registry.getReadyServer("the_room").getName().equals(preferred.getName()))
                throw new Exception("Room with more hits not preferred.");
        }
    }


    @Test
    public void shouldPreferServerWithRoomLoaded() throws Exception {
        RegistryService registry = new RegistryService();
        Server server = new Server("not_this", "", 80);
        Server preferred = new Server("the_one", "2", 80);
        registry.addServer(server);
        registry.addServer(preferred);
        registry.addRoom(preferred.getName(), new Room("mogul"));

        for (int i = 0; i < 1000; i++) {
            if (!registry.getReadyServer("mogul").getName().equals(preferred.getName()))
                throw new Exception("Server with room loaded not preferred.");
        }
    }

}
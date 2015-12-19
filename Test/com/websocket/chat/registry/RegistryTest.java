package com.websocket.chat.registry;

import com.websocket.chat.registry.Protocol.*;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocket;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;


/**
 * Created by Robin on 2015-12-18.
 * <p>
 * Tests for the Registry service.
 */

@RunWith(VertxUnitRunner.class)
public class RegistryTest {
    private Vertx vertx;

    @Rule
    public Timeout timeout = new Timeout(10000);

    @Rule
    public RepeatRule rule = new RepeatRule();

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void shouldRegisterServer(TestContext context) {
        final Async async = context.async();

        getConnectorSocket(connector -> {

            getServiceClientSocket(client -> {
                client.handler(data -> {
                    Index index = (Index) Serializer.unpack(data.toString(), Index.class);

                    context.assertEquals(index.getName(), "registrytester.1");
                    context.assertEquals(index.getIp(), "localhost");
                    context.assertEquals(index.getPort(), 6767);

                    async.complete();
                });
                sendBus(client.textHandlerID(), new Lookup("room"));
            });
            sendBus(connector.textHandlerID(),
                    new ServerEvent()
                            .setName("registrytester.1")
                            .setIp("localhost")
                            .setPort(6767)
                            .setStatus(ServerEvent.ServerStatus.UP));
        });
    }

    @Test
    public void shouldUnregisterServer(TestContext context) {
        final Async async = context.async();

        getConnectorSocket(connector -> {

            getServiceClientSocket(client -> {
                client.handler(data -> {
                    Index index = (Index) Serializer.unpack(data.toString(), Index.class);
                    context.assertTrue(index.getFull());
                    async.complete();
                });
                sendBus(client.textHandlerID(), new Lookup("room"));
            });
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.UP));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.DOWN));
        });
    }


    private ServerEvent serverEvent(ServerEvent.ServerStatus status) {
        return new ServerEvent("registry.tester.1", status);
    }

    @Test
    public void shouldNotBalanceToFull(TestContext context) {
        final Async async = context.async();

        getConnectorSocket(connector -> {

            getServiceClientSocket(client -> {
                client.handler(data -> {
                    Index index = (Index) Serializer.unpack(data.toString(), Index.class);
                    context.assertTrue(index.getFull());
                    async.complete();
                });
                sendBus(client.textHandlerID(), new Lookup("room"));
            });
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.UP));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.FULL));
        });
    }

    @Test
    public void shouldReEnableWhenReady(TestContext context) {
        final Async async = context.async();

        getConnectorSocket(connector -> {

            getServiceClientSocket(client -> {
                client.handler(data -> {
                    Index index = (Index) Serializer.unpack(data.toString(), Index.class);
                    context.assertFalse(index.getFull());
                    async.complete();
                });
                sendBus(client.textHandlerID(), new Lookup("room"));
            });
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.UP));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.FULL));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.READY));
        });
    }

    @Repeat(10)
    @Test
    public void shouldPreferServerWithRoomLoaded(TestContext context) {
        final Async async = context.async();

        getConnectorSocket(connector -> {

            getServiceClientSocket(client -> {
                client.handler(data -> {
                    Index index = (Index) Serializer.unpack(data.toString(), Index.class);
                    context.assertEquals(index.getName(), "registry.tester.2");
                    async.complete();
                });
                sendBus(client.textHandlerID(), new Lookup("room"));
            });
            sendBus(connector.textHandlerID(), new ServerEvent("registry.tester.1", ServerEvent.ServerStatus.UP));
            sendBus(connector.textHandlerID(), new ServerEvent("registry.tester.2", ServerEvent.ServerStatus.UP));
            sendBus(connector.textHandlerID(), new RoomEvent("registry.tester.2", "room", RoomEvent.RoomStatus.POPULATED));
        });
    }


    @Test
    /**
     * Hard to test as the first available server is selected when more than one room is populated.
     * Therefore we populate two rooms, send the down signal on the first, and generate some hits on the second.
     * The first server is now up and running again, but should be lower prioritized.
     */
    public void shouldPreferServerWithMoreHits(TestContext context) {
        final Async async = context.async();
        final CountDown counter = new CountDown(20);

        getConnectorSocket(connector -> {
            sendBus(connector.textHandlerID(), new ServerEvent("registry.tester.1", ServerEvent.ServerStatus.UP));
            sendBus(connector.textHandlerID(), new ServerEvent("registry.tester.2", ServerEvent.ServerStatus.UP));
            sendBus(connector.textHandlerID(), new RoomEvent("registry.tester.1", "room", RoomEvent.RoomStatus.POPULATED));
            sendBus(connector.textHandlerID(), new RoomEvent("registry.tester.2", "room", RoomEvent.RoomStatus.POPULATED));
            sendBus(connector.textHandlerID(), new ServerEvent("registry.tester.1", ServerEvent.ServerStatus.FULL));

            getServiceClientSocket(client -> {
                sendBus(client.textHandlerID(), new Lookup("room"));

                client.handler(response -> {
                    sendBus(connector.textHandlerID(), new ServerEvent("registry.tester.1", ServerEvent.ServerStatus.READY));
                });
            });

            getServiceClientSocket(client -> {
                client.handler(data -> {
                    Index index = (Index) Serializer.unpack(data.toString(), Index.class);

                    context.assertEquals(index.getName(), "registry.tester.2");

                    if (counter.down() == 0)
                        async.complete();
                });

                for (int i = 0; i < counter.left(); i++)
                    sendBus(client.textHandlerID(), new Lookup("room"));
            });
        });
    }

    public void sendBus(String address, Object message) {
        vertx.eventBus().send(address, Serializer.pack(message));
    }

    public void getConnectorBuffer(Handler<Buffer> handler) {
        vertx.createHttpClient().websocket(Launcher.CONNECTOR_PORT, "localhost", "/", event -> {
            event.handler(handler);
        });
    }

    public void getConnectorSocket(Handler<WebSocket> handler) {
        vertx.createHttpClient().websocket(Launcher.CONNECTOR_PORT, "localhost", "/", handler);
    }


    public void getServiceClientBuffer(Handler<Buffer> handler) {
        vertx.createHttpClient().websocket(Launcher.CLIENT_PORT, "localhost", "/", event -> {
            event.handler(handler);
        });
    }

    public void getServiceClientSocket(Handler<WebSocket> handler) {
        vertx.createHttpClient().websocket(Launcher.CLIENT_PORT, "localhost", "/", handler);
    }
}

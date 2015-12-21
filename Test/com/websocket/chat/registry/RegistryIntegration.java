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
public class RegistryIntegration {
    private Vertx vertx;

    @Rule
    public Timeout timeout = new Timeout(1000);

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

                    context.assertEquals(index.getName(), "registry.tester.1");
                    context.assertEquals(index.getIp(), "localhost");
                    context.assertEquals(index.getPort(), 6767);
                    unregister(connector.textHandlerID(), 1);
                    async.complete();
                });
                sendBus(client.textHandlerID(), new Lookup("room"));
            });
            sendBus(connector.textHandlerID(),
                    new ServerEvent()
                            .setName("registry.tester.1")
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
                    unregister(connector.textHandlerID(),1);
                    async.complete();
                });
                sendBus(client.textHandlerID(), new Lookup("room"));
            });
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.UP, 1));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.FULL, 1));
        });
    }


    private ServerEvent serverEvent(ServerEvent.ServerStatus status, Integer id) {
        return new ServerEvent("registry.tester." + id, status);
    }

    private void unregister(String address, Integer id) {
        sendBus(address, serverEvent(ServerEvent.ServerStatus.DOWN, id));
    }

    @Test
    public void shouldNotBalanceToFull(TestContext context) {
        final Async async = context.async();

        getConnectorSocket(connector -> {

            getServiceClientSocket(client -> {
                client.handler(data -> {

                    System.out.println(data.toString());

                    Index index = (Index) Serializer.unpack(data.toString(), Index.class);
                    context.assertTrue(index.getFull());
                    unregister(connector.textHandlerID(), 1);
                    async.complete();
                });
                sendBus(client.textHandlerID(), new Lookup("room"));
            });
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.UP, 1));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.FULL, 1));
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
                    unregister(connector.textHandlerID(), 1);
                    async.complete();
                });
                sendBus(client.textHandlerID(), new Lookup("room"));
            });
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.UP, 1));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.FULL, 1));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.READY, 1));
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
                    unregister(connector.textHandlerID(), 2);
                    unregister(connector.textHandlerID(), 1);
                    async.complete();
                });
                sendBus(client.textHandlerID(), new Lookup("room"));
            });
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.UP, 1));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.UP, 2));
            sendBus(connector.textHandlerID(), roomEvent(RoomEvent.RoomStatus.POPULATED, "room", 2));
        });
    }

    private RoomEvent roomEvent(RoomEvent.RoomStatus status, String room, Integer id) {
        return new RoomEvent("registry.tester." + id, room, status);
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
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.UP, 1));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.UP, 2));
            sendBus(connector.textHandlerID(), roomEvent(RoomEvent.RoomStatus.POPULATED, "room", 1));
            sendBus(connector.textHandlerID(), roomEvent(RoomEvent.RoomStatus.POPULATED, "room", 2));
            sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.FULL, 1));

            getServiceClientSocket(client -> {
                sendBus(client.textHandlerID(), new Lookup("room"));

                client.handler(response -> {
                    sendBus(connector.textHandlerID(), serverEvent(ServerEvent.ServerStatus.READY, 1));
                });
            });

            getServiceClientSocket(client -> {
                client.handler(data -> {
                    Index index = (Index) Serializer.unpack(data.toString(), Index.class);

                    context.assertEquals(index.getName(), "registry.tester.2");

                    if (counter.down() == 0) {
                        unregister(connector.textHandlerID(), 1);
                        unregister(connector.textHandlerID(), 2);
                        async.complete();
                    }
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
        vertx.createHttpClient().websocket(Configuration.CONNECTOR_PORT, "localhost", "/", event -> {
            event.handler(handler);
        });
    }

    public void getConnectorSocket(Handler<WebSocket> handler) {
        vertx.createHttpClient().websocket(Configuration.CONNECTOR_PORT, "localhost", "/", handler);
    }


    public void getServiceClientBuffer(Handler<Buffer> handler) {
        vertx.createHttpClient().websocket(Configuration.CLIENT_PORT, "localhost", "/", event -> {
            event.handler(handler);
        });
    }

    public void getServiceClientSocket(Handler<WebSocket> handler) {
        vertx.createHttpClient().websocket(Configuration.CLIENT_PORT, "localhost", "/", handler);
    }
}

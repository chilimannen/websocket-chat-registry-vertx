package com.websocket.chat.registry;

import io.vertx.core.*;

/**
 * @author Robin Duda
 *         <p>
 *         Loader class for verticle startup.
 */

public class Launcher extends AbstractVerticle {
    public final static Integer CLIENT_PORT = 6090;
    public final static Integer CONNECTOR_PORT = 7040;
    private Vertx vertx;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.deployVerticle(new RegistryService());
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
    }
}

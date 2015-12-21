package com.websocket.chat.registry;

import io.vertx.core.*;

/**
 * @author Robin Duda
 *         <p>
 *         Loader class for verticle startup.
 */

public class Launcher extends AbstractVerticle {
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

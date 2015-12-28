package com.websocket.chat.registry;

/**
 * Created by Robin on 2015-12-21.
 * <p>
 * Contains configuration params.
 */
public class Configuration {
    public final static Integer CLIENT_PORT = 6090;
    public final static Integer CONNECTOR_PORT = 7040;
    public static final Integer LOGGER_PORT = 5454;
    public static final String BUS_LOGGING = "logger.upstream";
    public static final long LOG_INTERVAL = 1000;
    public static final String REGISTER_NAME = "registry";
}

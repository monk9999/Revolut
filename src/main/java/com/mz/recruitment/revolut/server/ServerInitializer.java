package com.mz.recruitment.revolut.server;

import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;

public class ServerInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerInitializer.class);
    private final List<HttpServer> httpServers;
    private final int serverPort;
    private final Vertx vertx;

    ServerInitializer(List<HttpServer> httpServers, int serverPort) {
        this.httpServers = httpServers;
        this.serverPort = serverPort;
        this.vertx = Vertx.vertx();
    }

    public void initializeVertxServer() {
        LOGGER.debug("Initialize and fill config.");
        Context context = vertx.getOrCreateContext();
        context.config().put(HttpServer.HTTP_PORT_CONFIG_FIELD_NAME, serverPort);
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(context.config());

        LOGGER.debug("Deploying server Verticles.");
        httpServers.forEach(httpServer -> vertx.deployVerticle(httpServer, deploymentOptions));
    }

    public void stopVertxServer() {
        vertx.close();
    }

}

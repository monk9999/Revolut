package com.mz.recruitment.revolut.server;

import com.mz.recruitment.revolut.server.handler.MoneyTransferRequestValidator;
import com.mz.recruitment.revolut.server.handler.MoneyTransferHandler;
import com.mz.recruitment.revolut.server.handler.SafeConverter;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpServer extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);
    static final String HTTP_PORT_CONFIG_FIELD_NAME = "http.port";
    private final MoneyTransferHandler moneyTransferHandler;
    private final MoneyTransferRequestValidator moneyTransferRequestValidator;
    private final SafeConverter safeConverter;

    HttpServer(MoneyTransferHandler moneyTransferHandler, MoneyTransferRequestValidator moneyTransferRequestValidator,
               SafeConverter safeConverter) {
        this.moneyTransferHandler = moneyTransferHandler;
        this.moneyTransferRequestValidator = moneyTransferRequestValidator;
        this.safeConverter = safeConverter;
    }

    @Override
    public void start(Future<Void> startFuture) {
        LOGGER.debug("Starting initialization of Vert.x http server.");
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        LOGGER.debug("Registering handlers.");
        router.post("/transfer")
                .consumes(HttpHeaderValues.APPLICATION_JSON.toString())
                .handler(safeConverter::safelyConvertToJson)
                .handler(moneyTransferRequestValidator::validate)
                .handler(safeConverter::safelyConvertToTransferRequest)
                .handler(moneyTransferHandler::handleTransferRequest)
                .failureHandler(this::handleFailure);

        LOGGER.debug("Handlers registered. Staring service.");
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger(HTTP_PORT_CONFIG_FIELD_NAME),
                        result -> {
                            if (result.succeeded()) {
                                LOGGER.info("Http server up and running.");
                                startFuture.complete();
                            } else {
                                startFuture.fail(result.cause());
                            }
                        }
                );
    }

    private void handleFailure(RoutingContext routingContext) {
        LOGGER.error("Unknown exception. Returning internal server error status.", routingContext.failure());
        routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
    }
}

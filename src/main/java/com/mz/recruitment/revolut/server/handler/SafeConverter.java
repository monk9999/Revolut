package com.mz.recruitment.revolut.server.handler;

import com.mz.recruitment.revolut.server.request.MoneyTransferRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import static io.netty.handler.codec.http.HttpResponseStatus.UNPROCESSABLE_ENTITY;

public class SafeConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeConverter.class);
    static final String CONVERTED_BODY = "convertedBody";
    static final String MONEY_TRANSFER_REQUEST = "moneyTransferRequest";

    public void safelyConvertToJson(RoutingContext routingContext) {
        try {
            LOGGER.debug("Converting raw request body to JSON.");
            JsonObject body = routingContext.getBodyAsJson();
            routingContext.put(CONVERTED_BODY, body);
        } catch (DecodeException e) {
            LOGGER.info("Malformed request. Returning error response.");
            routingContext.response().setStatusCode(UNPROCESSABLE_ENTITY.code()).end("Malformed request.");
            return;
        }
        LOGGER.debug("Body successfully converted. Proceed with next handler.");
        routingContext.next();
    }

    public void safelyConvertToTransferRequest(RoutingContext routingContext) {
        LOGGER.debug("Converting Json request to MoneyTransferRequest.");
        JsonObject body = routingContext.get(SafeConverter.CONVERTED_BODY);
        MoneyTransferRequest moneyTransferRequest = MoneyTransferRequest.fromJson(body);
        routingContext.put(MONEY_TRANSFER_REQUEST, moneyTransferRequest);
        LOGGER.debug("Conversion complete. Proceed with next handler.");
        routingContext.next();
    }

}

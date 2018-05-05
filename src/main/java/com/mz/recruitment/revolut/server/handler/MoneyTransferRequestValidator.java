package com.mz.recruitment.revolut.server.handler;

import com.mz.recruitment.revolut.server.request.MoneyTransferRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.util.Strings;

import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.UNPROCESSABLE_ENTITY;

public class MoneyTransferRequestValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoneyTransferRequestValidator.class);
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\d+(.\\d{0,2})?");

    public void validate(RoutingContext routingContext) {
        LOGGER.debug("Starting validation.");
        JsonObject body = routingContext.get(SafeConverter.CONVERTED_BODY);

        Object fromAccount = body.getValue(MoneyTransferRequest.FROM_ACCOUNT_FIELD_NAME);
        if (isNullOrEmpty(fromAccount)) {
            LOGGER.info("Missing 'fromAccount' field. Returning error response");
            routingContext.response().setStatusCode(UNPROCESSABLE_ENTITY.code()).end("Missing 'fromAccount' field");
            return;
        }
        Object toAccount = body.getValue(MoneyTransferRequest.TO_ACCOUNT_FIELD_NAME);
        if (isNullOrEmpty(toAccount)) {
            LOGGER.info("Missing 'toAccount' field. Returning error response");
            routingContext.response().setStatusCode(UNPROCESSABLE_ENTITY.code()).end("Missing 'toAccount' field");
            return;
        }
        if (fromAccount.equals(toAccount)) {
            LOGGER.info("Source account and destination account are the same. Returning error response");
            routingContext.response().setStatusCode(HttpResponseStatus.UNPROCESSABLE_ENTITY.code())
                    .end("'fromAccount' same as 'toAccount'.");
            return;
        }
        Object amount = body.getValue(MoneyTransferRequest.AMOUNT_FILED_NAME);
        if (isNullOrEmpty(amount)) {
            LOGGER.info("Missing 'amount' field. Returning error response");
            routingContext.response().setStatusCode(UNPROCESSABLE_ENTITY.code()).end("Missing 'amount' field");
            return;
        }
        if (!AMOUNT_PATTERN.matcher(amount.toString()).matches()) {
            LOGGER.info("'amount' field value doesn't represent valid number. Returning error response");
            routingContext.response().setStatusCode(UNPROCESSABLE_ENTITY.code())
                    .end("'amount' field is not representing numerical value");
            return;
        }
        LOGGER.debug("Validation complete. Proceed to the next handler.");
        routingContext.next();
    }

    private boolean isNullOrEmpty(Object value) {
        return value == null || Strings.EMPTY.equals(value.toString());
    }
}

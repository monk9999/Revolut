package com.mz.recruitment.revolut.server.handler;

import com.google.common.collect.Lists;
import com.mz.recruitment.revolut.model.transfer.TransferResult;
import com.mz.recruitment.revolut.server.request.MoneyTransferRequest;
import com.mz.recruitment.revolut.service.AccountService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.RoutingContext;

import java.util.Collections;
import java.util.List;

public class MoneyTransferHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoneyTransferHandler.class);
    private final AccountService accountService;

    public MoneyTransferHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    public void handleTransferRequest(RoutingContext routingContext) {
        LOGGER.debug("Beginning transfer request handle.");
        MoneyTransferRequest moneyTransferRequest = routingContext.get(SafeConverter.MONEY_TRANSFER_REQUEST);
        SharedData sharedData = routingContext.vertx().sharedData();

        List<String> accounts = Lists.newArrayList(moneyTransferRequest.getFromAccount(),
                moneyTransferRequest.getToAccount());
        Collections.sort(accounts);

        LOGGER.debug("Acquired correct order of locks. Begin locking.");
        lockAccountsAndMakeTransfer(routingContext, sharedData, accounts.get(0), accounts.get(1), moneyTransferRequest);
    }

    private void makeTransfer(RoutingContext routingContext, MoneyTransferRequest moneyTransferRequest) {
        TransferResult transferResult = accountService.makeTransfer(moneyTransferRequest.getFromAccount(),
                moneyTransferRequest.getToAccount(), moneyTransferRequest.getAmount());
        if (TransferResult.SUCCESS == transferResult) {
            LOGGER.info("Transfer completed successfully.");
            routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
            return;
        }
        if (TransferResult.INSUFFICIENT_FUNDS == transferResult) {
            LOGGER.info("Transfer failed. Insufficient funds");
            routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end("Insufficient funds.");
            return;
        }
        if (TransferResult.SENDER_NOT_EXIST == transferResult) {
            LOGGER.info("Transfer failed. Sender account not exists in db.");
            routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end("Account from money " +
                    "should be transferred does not exist.");
            return;
        }
        if (TransferResult.RECEIVER_NOT_EXIST == transferResult) {
            LOGGER.info("Transfer failed. Recipient account not exists in db.");
            routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end("Money receiver " +
                    "account does not exist.");
            return;
        }
        throw new IllegalStateException("Unknown transfer result: " + transferResult.name());
    }

    private void lockAccountsAndMakeTransfer(RoutingContext routingContext, SharedData sharedData, String firstLockName,
                                             String secondLockName, MoneyTransferRequest moneyTransferRequest) {
        LOGGER.debug("Acquiring first lock.");
        sharedData.getLock(firstLockName, result -> {
            if (result.succeeded()) {
                LOGGER.debug("First lock acquired.");
                executeTransferInIsolation(routingContext, sharedData, secondLockName, moneyTransferRequest, result
                        .result());
            } else {
                LOGGER.error("Failed to acquire lock.", result.cause());
                routingContext.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            }
        });
    }

    private void executeTransferInIsolation(RoutingContext routingContext, SharedData sharedData, String lockName,
                                            MoneyTransferRequest moneyTransferRequest, Lock lockToRelease) {
        LOGGER.debug("Acquiring second lock.");
        sharedData.getLock(lockName, result -> {
            if (result.succeeded()) {
                Lock innerLock = result.result();
                try {
                    LOGGER.debug("Second lock acquired. Proceed to money transfer.");
                    makeTransfer(routingContext, moneyTransferRequest);
                } finally {
                    LOGGER.debug("Releasing locks.");
                    innerLock.release();
                    lockToRelease.release();
                    LOGGER.debug("Locks released.");
                }
            } else {
                LOGGER.error("Failed to acquire second lock.", result.cause());
                lockToRelease.release();
                routingContext.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            }
        });
    }
}

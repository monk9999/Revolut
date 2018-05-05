package com.mz.recruitment.revolut.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mz.recruitment.revolut.server.handler.MoneyTransferHandler;
import com.mz.recruitment.revolut.server.handler.MoneyTransferRequestValidator;
import com.mz.recruitment.revolut.server.handler.SafeConverter;
import com.mz.recruitment.revolut.service.AccountService;
import com.mz.recruitment.revolut.service.ServiceModule;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerModule.class);

    @Override
    protected void configure() {
        install(new ServiceModule());
    }

    @Provides
    @Singleton
    public ServerInitializer getServerInitializer(@Named("http.port") int serverPort, List<HttpServer> httpServer) {
        return new ServerInitializer(httpServer, serverPort);
    }

    @Provides
    public List<HttpServer> getHttpServers(@Named("http.server.instances") int serverInstances, AccountService
            accountService) {
        MoneyTransferHandler moneyTransferHandler = new MoneyTransferHandler(accountService);
        MoneyTransferRequestValidator moneyTransferRequestValidator = new MoneyTransferRequestValidator();
        SafeConverter safeConverter = new SafeConverter();
        return IntStream.range(0, calculateServerInstances(serverInstances))
                .mapToObj(i -> new HttpServer(moneyTransferHandler, moneyTransferRequestValidator, safeConverter))
                .collect(Collectors.toList());
    }

    private int calculateServerInstances(int serverInstances) {
        int result = serverInstances == 0 ? Runtime.getRuntime().availableProcessors() : serverInstances;
        LOGGER.debug("Calculated server instances: {}", result);
        return result;
    }
}

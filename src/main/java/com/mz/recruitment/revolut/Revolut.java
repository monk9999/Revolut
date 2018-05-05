package com.mz.recruitment.revolut;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mz.recruitment.revolut.model.AccountFactory;
import com.mz.recruitment.revolut.repository.AccountRepository;
import com.mz.recruitment.revolut.server.ServerInitializer;

import java.math.BigDecimal;

public final class Revolut {

    private Revolut() {
    }

    public static void main(String... args) {
        System.setProperty("vertx.logger-delegate-factory-class-name",
                "io.vertx.core.logging.Log4j2LogDelegateFactory");
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

        Injector injector = Guice.createInjector(new ApplicationModule());
        setUp(injector.getInstance(AccountRepository.class));
        injector.getInstance(ServerInitializer.class).initializeVertxServer();
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private static void setUp(AccountRepository accountRepository) {
        accountRepository.save(AccountFactory.getAccountFactory().createEmptyAccount("01"));
        accountRepository.save(AccountFactory.getAccountFactory().createEmptyAccount("02"));
        accountRepository.save(AccountFactory.getAccountFactory().createEmptyAccount("03"));
        accountRepository.save(AccountFactory.getAccountFactory().createEmptyAccount("04"));
        accountRepository.save(AccountFactory.getAccountFactory().createEmptyAccount("05"));
        accountRepository.save(AccountFactory.getAccountFactory().createAccountWithBalance("1",
                BigDecimal.valueOf(100000)));
        accountRepository.save(AccountFactory.getAccountFactory().createAccountWithBalance("2",
                BigDecimal.valueOf(200000)));
        accountRepository.save(AccountFactory.getAccountFactory().createAccountWithBalance("3",
                BigDecimal.valueOf(300000)));
        accountRepository.save(AccountFactory.getAccountFactory().createAccountWithBalance("4",
                BigDecimal.valueOf(400000)));
        accountRepository.save(AccountFactory.getAccountFactory().createAccountWithBalance("5",
                BigDecimal.valueOf(500000)));
    }
}

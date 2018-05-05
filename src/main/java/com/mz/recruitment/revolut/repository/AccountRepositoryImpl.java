package com.mz.recruitment.revolut.repository;

import com.mz.recruitment.revolut.model.Account;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AccountRepositoryImpl implements AccountRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRepository.class);
    private final Map<String, Account> accounts = new HashMap<>();

    @Override
    public Optional<Account> getByNumber(String accountNumber) {
        LOGGER.debug("Retrieving account from repository.");
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    @Override
    public void save(Account account) {
        LOGGER.debug("Saving account.");
        accounts.put(account.getAccountNumber(), account);
        LOGGER.debug("Account saved.");
    }
}

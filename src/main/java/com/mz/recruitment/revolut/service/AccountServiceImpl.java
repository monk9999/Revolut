package com.mz.recruitment.revolut.service;

import com.mz.recruitment.revolut.model.Account;
import com.mz.recruitment.revolut.model.transfer.TransferResult;
import com.mz.recruitment.revolut.repository.AccountRepository;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

public class AccountServiceImpl implements AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final AccountRepository accountRepository;

    AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public TransferResult makeTransfer(String from, String to, BigDecimal amount) {
        LOGGER.debug("Retrieving accounts for transfer.");
        Optional<Account> fromAccount = accountRepository.getByNumber(from);
        Optional<Account> toAccount = accountRepository.getByNumber(to);
        if (!fromAccount.isPresent()) {
            LOGGER.debug("Sender account not found.");
            return TransferResult.SENDER_NOT_EXIST;
        }
        if (!toAccount.isPresent()) {
            LOGGER.debug("Recipient account not found.");
            return TransferResult.RECEIVER_NOT_EXIST;
        }
        LOGGER.debug("Proceeding with transfer.");
        return fromAccount.get().transferTo(toAccount.get(), amount);
    }
}

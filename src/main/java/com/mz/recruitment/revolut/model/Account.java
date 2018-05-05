package com.mz.recruitment.revolut.model;

import com.mz.recruitment.revolut.model.transfer.TransferResult;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;

public class Account implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Account.class);
    private final String accountNumber;
    private BigDecimal balance;

    public Account(String accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public TransferResult transferTo(Account account, BigDecimal amount) {
        LOGGER.debug("Begin transfer of funds.");
        if (hasSufficientMoney(amount)) {
            LOGGER.debug("Account has sufficient money.");
            this.balance = this.balance.subtract(amount);
            account.receiveMoney(amount);
            LOGGER.debug("Transfer was successful.");
            return TransferResult.SUCCESS;
        }
        LOGGER.debug("Transfer failed. Insufficient money on the account.");
        return TransferResult.INSUFFICIENT_FUNDS;
    }

    private void receiveMoney(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    private boolean hasSufficientMoney(BigDecimal amount) {
        return this.balance.subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        return accountNumber != null ? accountNumber.equals(account.accountNumber) : account.accountNumber == null;
    }

    @Override
    public int hashCode() {
        return accountNumber != null ? accountNumber.hashCode() : 0;
    }
}

package com.mz.recruitment.revolut.model;


import java.math.BigDecimal;

public final class AccountFactory {
    private static final AccountFactory INSTANCE = new AccountFactory();

    private AccountFactory() {
    }

    public static AccountFactory getAccountFactory() {
        return INSTANCE;
    }

    public Account createEmptyAccount(String accountNumber) {
        return new Account(accountNumber, BigDecimal.ZERO);
    }

    public Account createAccountWithBalance(String accountNumber, BigDecimal balance) {
        return new Account(accountNumber, balance);
    }

}

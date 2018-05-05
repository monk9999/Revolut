package com.mz.recruitment.revolut.repository;

import com.mz.recruitment.revolut.model.Account;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> getByNumber(String accountNumber);

    void save(Account account);

}

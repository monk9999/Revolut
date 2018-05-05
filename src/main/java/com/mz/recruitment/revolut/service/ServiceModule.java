package com.mz.recruitment.revolut.service;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mz.recruitment.revolut.repository.AccountRepository;
import com.mz.recruitment.revolut.repository.RepositoryModule;

public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new RepositoryModule());
    }

    @Provides
    public AccountService getAccountService(AccountRepository accountRepository) {
        return new AccountServiceImpl(accountRepository);
    }
}

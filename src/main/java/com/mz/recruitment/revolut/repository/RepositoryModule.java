package com.mz.recruitment.revolut.repository;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class RepositoryModule extends AbstractModule {

    @Provides
    @Singleton
    public AccountRepository getAccountRepository() {
        return new AccountRepositoryImpl();
    }
}

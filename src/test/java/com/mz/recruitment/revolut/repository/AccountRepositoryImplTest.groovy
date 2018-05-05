package com.mz.recruitment.revolut.repository

import com.mz.recruitment.revolut.model.AccountFactory
import spock.lang.Specification

class AccountRepositoryImplTest extends Specification {

    def accountRepository = new AccountRepositoryImpl()

        def "get account when is present"() {
        given: "example account in database"
        def exampleAccountNumber = "exampleAccountNumber"
        def exampleAccount = AccountFactory.accountFactory.createEmptyAccount(exampleAccountNumber)
        accountRepository.save(exampleAccount)

        when: "account is searched by number"
        def result = accountRepository.getByNumber(exampleAccountNumber)

        then: "retrieved account is present"
        result.isPresent()
        and: "it is the same as exampleAccount"
        exampleAccount == result.get()
    }
}

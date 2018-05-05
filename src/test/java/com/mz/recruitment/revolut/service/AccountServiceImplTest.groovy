package com.mz.recruitment.revolut.service

import com.mz.recruitment.revolut.model.AccountFactory
import com.mz.recruitment.revolut.repository.AccountRepository
import spock.lang.Specification

class AccountServiceImplTest extends Specification {

    def firstAccountNumber = "firstAccountNumber"
    def secondAccountNumber = "secondAccountNumber"
    def accountRepository = Mock(AccountRepository)
    def fistAccount = AccountFactory.getAccountFactory().createAccountWithBalance(firstAccountNumber, BigDecimal.TEN)
    def secondAccount = AccountFactory.getAccountFactory().createAccountWithBalance(secondAccountNumber, BigDecimal.TEN)
    def accountService = new AccountServiceImpl(accountRepository)

    def setup(){
        accountRepository.getByNumber(firstAccountNumber) >> Optional.of(fistAccount)
        accountRepository.getByNumber(secondAccountNumber) >> Optional.of(secondAccount)
        accountRepository.getByNumber(_) >> Optional.empty()
    }

    def "should make transfer when accounts are present"(){
        when: "transfer is make"
        accountService.makeTransfer(firstAccountNumber, secondAccountNumber, BigDecimal.ONE)

        then: "money were exchanged"
        fistAccount.getBalance() == BigDecimal.valueOf(9)
        secondAccount.getBalance() == BigDecimal.valueOf(11)
    }

    def "should not transfer when first account is not present"(){
        when: "transfer is make"
        accountService.makeTransfer("notExistingAccountNumber", secondAccountNumber, BigDecimal.ONE)

        then: "money were exchanged"
        fistAccount.getBalance() == BigDecimal.valueOf(10)
        secondAccount.getBalance() == BigDecimal.valueOf(10)
    }

    def "should not transfer when ssecond account is not present"(){
        when: "transfer is make"
        accountService.makeTransfer(firstAccountNumber, "notExistingAccount", BigDecimal.ONE)

        then: "money were exchanged"
        fistAccount.getBalance() == BigDecimal.valueOf(10)
        secondAccount.getBalance() == BigDecimal.valueOf(10)
    }

}

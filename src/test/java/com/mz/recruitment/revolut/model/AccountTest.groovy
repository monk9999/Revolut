package com.mz.recruitment.revolut.model

import com.mz.recruitment.revolut.model.transfer.TransferResult
import spock.lang.Specification

class AccountTest extends Specification {

    def "should transfer money when there are sufficient amount on account"(){
        given: "account with money"
        def accountWithMoney = AccountFactory.getAccountFactory().createAccountWithBalance("accountWithMoney", BigDecimal.valueOf(100))
        and: "another that is empty"
        def emptyAccount = AccountFactory.getAccountFactory().createEmptyAccount("emptyAccount")

        when: "transfer for 50 is made"
        def transferResult = accountWithMoney.transferTo(emptyAccount, BigDecimal.valueOf(50))

        then: "account balance is 50"
        emptyAccount.getBalance() == BigDecimal.valueOf(50)
        and: "transaction status is success"
        transferResult == TransferResult.SUCCESS
    }

    def "should not transfer money when there are inSufficient amount on account"(){
        given: "account with money"
        def accountWithMoney = AccountFactory.getAccountFactory().createAccountWithBalance("accountWithMoney", BigDecimal.valueOf(100))
        and: "another that is empty"
        def emptyAccount = AccountFactory.getAccountFactory().createEmptyAccount("emptyAccount")

        when: "transfer for 50 is made"
        def transferResult = accountWithMoney.transferTo(emptyAccount, BigDecimal.valueOf(1000))

        then: "account balance is 100"
        accountWithMoney.getBalance() == BigDecimal.valueOf(100)
        and: "empty account stays empty"
        emptyAccount.getBalance() == BigDecimal.ZERO
        and: "transaction status is success"
        transferResult == TransferResult.INSUFFICIENT_FUNDS
    }

}

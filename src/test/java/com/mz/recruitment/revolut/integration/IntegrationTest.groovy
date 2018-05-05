package com.mz.recruitment.revolut.integration

import com.google.inject.Guice
import com.mz.recruitment.revolut.ApplicationModule
import com.mz.recruitment.revolut.model.AccountFactory
import com.mz.recruitment.revolut.repository.AccountRepository
import com.mz.recruitment.revolut.server.ServerInitializer
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class IntegrationTest extends Specification {

    def "should make simple transfer when application is ready"() {
        given: "request"
        def requestBody = "{\"fromAccount\":\"exampleAccount1\", \"toAccount\":\"exampleAccount2\", \"amount\":1}"
        and: "filled database"
        def injector = Guice.createInjector(new ApplicationModule())
        def repository = injector.getInstance(AccountRepository.class)
        repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("exampleAccount1", BigDecimal.TEN))
        repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("exampleAccount2", BigDecimal.TEN))
        and: "running application"
        injector.getInstance(ServerInitializer.class).initializeVertxServer()

        when: "executing request"
        HttpURLConnection connection = executePostRequest(requestBody)

        then: "result should be 200"
        connection.getResponseCode() == 200
    }

    private def executePostRequest(String requestBody) {
        def url = new URL("http://localhost:7777/transfer")
        def connection = (HttpURLConnection) url.openConnection()
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        connection.setFixedLengthStreamingMode(requestBody.length())
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.connect()
        connection.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8))
        connection
    }
}


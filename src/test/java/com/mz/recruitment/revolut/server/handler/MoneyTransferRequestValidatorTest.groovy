package com.mz.recruitment.revolut.server.handler

import com.mz.recruitment.revolut.server.request.MoneyTransferRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import spock.lang.Specification

class MoneyTransferRequestValidatorTest extends Specification {

    def moneyTransferRequestValidator = new MoneyTransferRequestValidator()
    def routingContext = Mock(RoutingContext)
    def response = Mock(HttpServerResponse)

    def setup() {
        routingContext.response() >> response
    }

    def "should end response with 422 status code when fromAccount is missing"() {
        given: "JSON object without 'fromAccount' field"
        def jsonObject = new JsonObject()
                .put(MoneyTransferRequest.TO_ACCOUNT_FIELD_NAME, "exampleValue",)
                .put(MoneyTransferRequest.AMOUNT_FILED_NAME, "1")
        routingContext.get(SafeConverter.CONVERTED_BODY) >> jsonObject

        when: "handler is invoked"
        moneyTransferRequestValidator.validate(routingContext)

        then: "response was ended with 422 code"
        1 * response.setStatusCode(422) >> response
        and: "routing was not passed later"
        0 * routingContext.next()
        noExceptionThrown()
    }

    def "should end response with 422 status code when toAccount is missing"() {
        given: "JSON object without 'toAccount' field"
        def jsonObject = new JsonObject()
                .put(MoneyTransferRequest.FROM_ACCOUNT_FIELD_NAME, "exampleValue",)
                .put(MoneyTransferRequest.AMOUNT_FILED_NAME, "1")
        routingContext.get(SafeConverter.CONVERTED_BODY) >> jsonObject

        when: "handler is invoked"
        moneyTransferRequestValidator.validate(routingContext)

        then: "response was ended with 422 code"
        1 * response.setStatusCode(422) >> response
        and: "routing was not passed later"
        0 * routingContext.next()
        noExceptionThrown()
    }

    def "should end response with 422 status code when amount is missing"() {
        given: "JSON object without 'amount' field"
        def jsonObject = new JsonObject()
                .put(MoneyTransferRequest.FROM_ACCOUNT_FIELD_NAME, "exampleValue1",)
                .put(MoneyTransferRequest.TO_ACCOUNT_FIELD_NAME, "exampleValue2",)
        routingContext.get(SafeConverter.CONVERTED_BODY) >> jsonObject

        when: "handler is invoked"
        moneyTransferRequestValidator.validate(routingContext)

        then: "response was ended with 422 code"
        1 * response.setStatusCode(422) >> response
        and: "routing was not passed later"
        0 * routingContext.next()
        noExceptionThrown()
    }

    def "should end response with 422 status code when amount is not valid number"() {
        given: "JSON object with invalid 'amount' field"
        def jsonObject = new JsonObject()
                .put(MoneyTransferRequest.FROM_ACCOUNT_FIELD_NAME, "exampleValue1",)
                .put(MoneyTransferRequest.TO_ACCOUNT_FIELD_NAME, "exampleValue2",)
                .put(MoneyTransferRequest.AMOUNT_FILED_NAME, "notValidNumber")
        routingContext.get(SafeConverter.CONVERTED_BODY) >> jsonObject

        when: "handler is invoked"
        moneyTransferRequestValidator.validate(routingContext)

        then: "response was ended with 422 code"
        1 * response.setStatusCode(422) >> response
        and: "routing was not passed later"
        0 * routingContext.next()
        noExceptionThrown()
    }

    def "should end response with 422 status code when accounts are the same"() {
        given: "JSON object with the same 'toAccount' and 'fromAccount' fields"
        def jsonObject = new JsonObject()
                .put(MoneyTransferRequest.FROM_ACCOUNT_FIELD_NAME, "sameValue",)
                .put(MoneyTransferRequest.TO_ACCOUNT_FIELD_NAME, "sameValue",)
                .put(MoneyTransferRequest.AMOUNT_FILED_NAME, "1")
        routingContext.get(SafeConverter.CONVERTED_BODY) >> jsonObject

        when: "handler is invoked"
        moneyTransferRequestValidator.validate(routingContext)

        then: "response was ended with 422 code"
        1 * response.setStatusCode(422) >> response
        and: "routing was not passed later"
        0 * routingContext.next()
        noExceptionThrown()
    }

    def "should proceed to next handler when object is valid"() {
        given: "JSON object with the same 'toAccount' and 'fromAccount' fields"
        def jsonObject = new JsonObject()
                .put(MoneyTransferRequest.FROM_ACCOUNT_FIELD_NAME, "exampleValue1",)
                .put(MoneyTransferRequest.TO_ACCOUNT_FIELD_NAME, "exampleValue2",)
                .put(MoneyTransferRequest.AMOUNT_FILED_NAME, "1")
        routingContext.get(SafeConverter.CONVERTED_BODY) >> jsonObject

        when: "handler is invoked"
        moneyTransferRequestValidator.validate(routingContext)

        then: "response was not ended with any code"
        0 * response.setStatusCode(_) >> response
        and: "routing was passed later"
        1 * routingContext.next()
        noExceptionThrown()
    }
}

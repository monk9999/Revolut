package com.mz.recruitment.revolut.server.handler

import com.mz.recruitment.revolut.server.request.MoneyTransferRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import spock.lang.Specification

class SafeConverterTest extends Specification {

    def safeConverter = new SafeConverter()

    def "should move to next handler when there are no problems"() {
        given: "JSON"
        def json = new JsonObject()
        and: "mocked context"
        def routingContext = Mock(RoutingContext)
        routingContext.getBodyAsJson() >> json

        when: "handle is invoked"
        safeConverter.safelyConvertToJson(routingContext)

        then: "routing is passed to the next handler"
        1 * routingContext.next()
        and: "no exception was thrown"
        noExceptionThrown()
    }

    def "should end response with status code when there are problems"() {
        given: "mocked context"
        def routingContext = Mock(RoutingContext)
        routingContext.getBodyAsJson() >> { throw new DecodeException() }
        and: "mocked response"
        def serverResponse = Mock(HttpServerResponse)
        routingContext.response() >> serverResponse

        when: "handle is invoked"
        safeConverter.safelyConvertToJson(routingContext)

        then: "routing is passed to the next handler"
        1 * serverResponse.setStatusCode(422) >> serverResponse
        and: "no exception was thrown"
        noExceptionThrown()
    }

    def "should convert to money transfer request when json is valid"(){
        given: "correct JSON representation of MoneyTransferRequest"
        def request = new MoneyTransferRequest("exampleFromAccount", "exampleToAccount", BigDecimal.ONE)
        def jsonObject = JsonObject.mapFrom(request)
        and: "mocked context"
        def routingContext = Mock(RoutingContext)
        routingContext.get(SafeConverter.CONVERTED_BODY) >> jsonObject

        when: "handle is invoked"
        safeConverter.safelyConvertToTransferRequest(routingContext)

        then: "request put in the context is the same"
        1 * routingContext.put(SafeConverter.MONEY_TRANSFER_REQUEST, request)
    }
}

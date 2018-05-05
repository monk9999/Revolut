package com.mz.recruitment.revolut.server.handler;

import com.mz.recruitment.revolut.model.transfer.TransferResult;
import com.mz.recruitment.revolut.server.request.MoneyTransferRequest;
import com.mz.recruitment.revolut.service.AccountService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//Must use Junit cause Spock stubbing methods are executed synchronously thus not allowing for contention test in
// parallel environment
@RunWith(MockitoJUnitRunner.class)
public class MoneyTransferHandlerTest {

    private AccountService accountService = mock(AccountService.class);
    private MoneyTransferHandler moneyTrasferHandler = new MoneyTransferHandler(accountService);
    private RoutingContext routingContextForFirstExecution = mock(RoutingContext.class);
    private RoutingContext routinbContextForSecondExecution = mock(RoutingContext.class);
    private AtomicBoolean contentionSwitch = new AtomicBoolean();
    private AtomicBoolean executionWasParallel = new AtomicBoolean();
    private Vertx vertx = Vertx.vertx();

    @Before
    public void setup() {
        HttpServerResponse httpServerResponse = mock(HttpServerResponse.class);
        when(httpServerResponse.setStatusCode(anyInt())).thenReturn(httpServerResponse);
        when(routingContextForFirstExecution.vertx()).thenReturn(vertx);
        when(routingContextForFirstExecution.response()).thenReturn(httpServerResponse);
        when(routinbContextForSecondExecution.vertx()).thenReturn(vertx);
        when(routinbContextForSecondExecution.response()).thenReturn(httpServerResponse);
    }

    @Test
    public void shouldNotBlock_whenRequestForDifferentAccounts() throws InterruptedException {
        //given: "two requests for different accounts"
        MoneyTransferRequest firstMoneyTransferRequest = new MoneyTransferRequest("exampleAccount1", "exampleAccount2",
                BigDecimal.ONE);
        when(routingContextForFirstExecution.get(SafeConverter.MONEY_TRANSFER_REQUEST)).thenReturn
                (firstMoneyTransferRequest);
        MoneyTransferRequest secondMoneyTransferRequest = new MoneyTransferRequest("differentAccount1",
                "differentAccount2", BigDecimal.ONE);
        when(routinbContextForSecondExecution.get(SafeConverter.MONEY_TRANSFER_REQUEST)).thenReturn
                (secondMoneyTransferRequest);
        //and: "thread pool for simultaneous execution"
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        //and: "fake method was provided that will switch parallel flag when executed in parallel"
        when(accountService.makeTransfer(anyString(), anyString(), any())).thenAnswer(invocationOnMock ->
                makeFakeTransferWithContentionCheck());

        //when: "handler is invoked simultaneously for both requests"
        threadPool.submit(() -> moneyTrasferHandler.handleTransferRequest(routingContextForFirstExecution));
        threadPool.submit(() -> moneyTrasferHandler.handleTransferRequest(routinbContextForSecondExecution));
        threadPool.awaitTermination(1, TimeUnit.SECONDS);

        //then: expect that parallel flag was set to true
        assertTrue(executionWasParallel.get());

    }

    @Test
    public void shouldBlock_whenRequestForAtLeastOneAccountIsTheSame() throws InterruptedException {
        //given: "two requests that have one same account"
        MoneyTransferRequest firstMoneyTransferRequest = new MoneyTransferRequest("exampleAccount1",
                "exampleSameAccount",
                BigDecimal.ONE);
        when(routingContextForFirstExecution.get(SafeConverter.MONEY_TRANSFER_REQUEST)).thenReturn
                (firstMoneyTransferRequest);
        MoneyTransferRequest secondMoneyTransferRequest = new MoneyTransferRequest("differentAccount1",
                "exampleSameAccount", BigDecimal.ONE);
        when(routinbContextForSecondExecution.get(SafeConverter.MONEY_TRANSFER_REQUEST)).thenReturn
                (secondMoneyTransferRequest);
        //and: "thread pool for simultaneous execution"
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        //and: "fake method was provided that will switch parallel flag when executed in parallel"
        when(accountService.makeTransfer(anyString(), anyString(), any())).thenAnswer(invocationOnMock ->
                makeFakeTransferWithContentionCheck());

        //when: "handler is invoked simultaneously for both requests"
        threadPool.submit(() -> moneyTrasferHandler.handleTransferRequest(routingContextForFirstExecution));
        threadPool.submit(() -> moneyTrasferHandler.handleTransferRequest(routinbContextForSecondExecution));
        threadPool.awaitTermination(1, TimeUnit.SECONDS);

        //then: expect that parallel flag was set to false
        assertFalse(executionWasParallel.get());

    }

    @Test
    public void shouldNotDeadLock_whenRequestForAccountsInterlaced() throws InterruptedException {
        //given: "two requests for interlaced accounts (sender is the same as recipient in other account)"
        MoneyTransferRequest firstMoneyTransferRequest = new MoneyTransferRequest("exampleSameAccount",
                "exampleAccount",
                BigDecimal.ONE);
        when(routingContextForFirstExecution.get(SafeConverter.MONEY_TRANSFER_REQUEST)).thenReturn
                (firstMoneyTransferRequest);
        MoneyTransferRequest secondMoneyTransferRequest = new MoneyTransferRequest("differentAccount1",
                "exampleSameAccount", BigDecimal.ONE);
        when(routinbContextForSecondExecution.get(SafeConverter.MONEY_TRANSFER_REQUEST)).thenReturn
                (secondMoneyTransferRequest);
        //and: "thread pool for simultaneous execution"
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        //and: "fake method was provided that that will switch parallel flag when executed in parallel"
        when(accountService.makeTransfer(anyString(), anyString(), any())).thenAnswer(invocationOnMock ->
                makeFakeTransferWithContentionCheck());

        //when: "handler is invoked simultaneously for both requests"
        threadPool.submit(() -> moneyTrasferHandler.handleTransferRequest(routingContextForFirstExecution));
        threadPool.submit(() -> moneyTrasferHandler.handleTransferRequest(routinbContextForSecondExecution));
        threadPool.awaitTermination(1, TimeUnit.SECONDS);

        //then: expect that parallel flag was set to false
        assertFalse(executionWasParallel.get());
        //and threads did not deadlocked and executed before pool termination
        verify(routingContextForFirstExecution.response(), times(2)).setStatusCode(200);

    }

    @Test
    public void shouldSet_whenInsufficientFounds() throws InterruptedException {
        //given example request
        MoneyTransferRequest transferRequest = new MoneyTransferRequest("exampleAccount1",
                "exampleAccount2", BigDecimal.ONE);
        when(routingContextForFirstExecution.get(SafeConverter.MONEY_TRANSFER_REQUEST)).thenReturn(transferRequest);
        //and method that will return insufficient founds status
        when(accountService.makeTransfer(anyString(), anyString(), any())).thenReturn(TransferResult.INSUFFICIENT_FUNDS);

        //when handler is invoked
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(()-> moneyTrasferHandler.handleTransferRequest(routingContextForFirstExecution));
        executorService.awaitTermination(100, TimeUnit.MILLISECONDS);

        //then 400 status code should be set on response
        verify(routingContextForFirstExecution.response()).setStatusCode(400);
        //and response was finished
        verify(routingContextForFirstExecution.response()).end(anyString());
    }

    @Test
    public void shouldSetBadRequestResponseStatus_whenSenderNotExistFounds() throws InterruptedException {
        //given example request
        MoneyTransferRequest transferRequest = new MoneyTransferRequest("notExistingSender",
                "exampleAccount2", BigDecimal.ONE);
        when(routingContextForFirstExecution.get(SafeConverter.MONEY_TRANSFER_REQUEST)).thenReturn(transferRequest);
        //and method that will return insufficient founds status
        when(accountService.makeTransfer(anyString(), anyString(), any())).thenReturn(TransferResult.SENDER_NOT_EXIST);

        //when handler is invoked
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(()-> moneyTrasferHandler.handleTransferRequest(routingContextForFirstExecution));
        executorService.awaitTermination(100, TimeUnit.MILLISECONDS);

        //then 400 status code should be set on response
        verify(routingContextForFirstExecution.response()).setStatusCode(400);
        //and response was finished
        verify(routingContextForFirstExecution.response()).end(anyString());
    }

    @Test
    public void shouldSetBadRequestResponseStatus_whenReceiverNotExist() throws InterruptedException {
        //given example request
        MoneyTransferRequest transferRequest = new MoneyTransferRequest("exampleAccount1",
                "notExistingReceiver", BigDecimal.ONE);
        when(routingContextForFirstExecution.get(SafeConverter.MONEY_TRANSFER_REQUEST)).thenReturn(transferRequest);
        //and method that will return insufficient founds status
        when(accountService.makeTransfer(anyString(), anyString(), any())).thenReturn(TransferResult.RECEIVER_NOT_EXIST);

        //when handler is invoked
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(()-> moneyTrasferHandler.handleTransferRequest(routingContextForFirstExecution));
        executorService.awaitTermination(100, TimeUnit.MILLISECONDS);

        //then 400 status code should be set on response
        verify(routingContextForFirstExecution.response()).setStatusCode(400);
        //and response was finished
        verify(routingContextForFirstExecution.response()).end(anyString());
    }

    @Test
    public void shouldSetOKStatus_whenTransferSucceded() throws InterruptedException {
        //given example request
        MoneyTransferRequest transferRequest = new MoneyTransferRequest("exampleAccount1",
                "exampleAccount2", BigDecimal.ONE);
        when(routingContextForFirstExecution.get(SafeConverter.MONEY_TRANSFER_REQUEST)).thenReturn(transferRequest);
        //and method that will return insufficient founds status
        when(accountService.makeTransfer(anyString(), anyString(), any())).thenReturn(TransferResult.SUCCESS);

        //when handler is invoked
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(()-> moneyTrasferHandler.handleTransferRequest(routingContextForFirstExecution));
        executorService.awaitTermination(100, TimeUnit.MILLISECONDS);

        //then 400 status code should be set on response
        verify(routingContextForFirstExecution.response()).setStatusCode(200);
        //and response was finished
        verify(routingContextForFirstExecution.response()).end();
    }


    private TransferResult makeFakeTransferWithContentionCheck() throws InterruptedException {
        contentionSwitch.set(true);
        Thread.sleep(100);
        if (!contentionSwitch.compareAndSet(true, false)) {
            executionWasParallel.set(true);
        }
        return TransferResult.SUCCESS;
    }
}
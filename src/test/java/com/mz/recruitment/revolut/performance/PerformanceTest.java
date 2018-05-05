package com.mz.recruitment.revolut.performance;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mz.recruitment.revolut.ApplicationModule;
import com.mz.recruitment.revolut.model.AccountFactory;
import com.mz.recruitment.revolut.repository.AccountRepository;
import com.mz.recruitment.revolut.server.ServerInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PerformanceTest {

    @Benchmark
    @BenchmarkMode(Mode.All)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void makeRequests(ServerState state, Blackhole blackhole) throws IOException {
        HttpURLConnection connection = executePostRequest(state.getRandomRequestBody());
        blackhole.consume(connection.getResponseMessage());
    }

    private HttpURLConnection executePostRequest(String requestBody) throws IOException {
        URL url = new URL("http://localhost:7777/transfer");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(requestBody.length());
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.connect();
        connection.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));
        return connection;
    }

    @State(Scope.Benchmark)
    public static class ServerState {
        List<String> requestBodyList = new ArrayList<>(10);
        private Random random = new Random();
        private ServerInitializer instance;

        @Setup(Level.Trial)
        public void setup() {

            System.setProperty("vertx.logger-delegate-factory-class-name",
                    "io.vertx.core.logging.Log4j2LogDelegateFactory");
            System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

            Injector injector = Guice.createInjector(new ApplicationModule());
            AccountRepository repository = injector.getInstance(AccountRepository.class);
            fillDB(repository);
            fillRequestBodyList();
            instance = injector.getInstance(ServerInitializer.class);
            instance.initializeVertxServer();
            Configurator.setAllLevels(LogManager.getRootLogger().getName(), org.apache.logging.log4j.Level.OFF);
        }

        @TearDown
        public void tearDown() {
            instance.stopVertxServer();
        }

        String getRandomRequestBody() {
            return requestBodyList.get(random.nextInt(10));
        }

        private void fillRequestBodyList() {
            addRequestToList("1", "2");
            addRequestToList("2", "3");
            addRequestToList("3", "4");
            addRequestToList("4", "5");
            addRequestToList("5", "6");
            addRequestToList("6", "7");
            addRequestToList("7", "8");
            addRequestToList("8", "9");
            addRequestToList("9", "10");
            addRequestToList("10", "1");
        }

        private void addRequestToList(String firstId, String secondId) {
            requestBodyList.add("{\"fromAccount\":\"" + firstId + "\", \"toAccount\":\"" + secondId + "\", " +
                    "\"amount\":1}");
        }

        private void fillDB(AccountRepository repository) {
            repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("1", BigDecimal.valueOf
                    (1000000)));
            repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("2", BigDecimal.valueOf
                    (1000000)));
            repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("3", BigDecimal.valueOf
                    (1000000)));
            repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("4", BigDecimal.valueOf
                    (1000000)));
            repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("5", BigDecimal.valueOf
                    (1000000)));
            repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("6", BigDecimal.valueOf
                    (1000000)));
            repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("7", BigDecimal.valueOf
                    (1000000)));
            repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("8", BigDecimal.valueOf
                    (1000000)));
            repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("9", BigDecimal.valueOf
                    (1000000)));
            repository.save(AccountFactory.getAccountFactory().createAccountWithBalance("10", BigDecimal.valueOf
                    (1000000)));
        }


    }
}

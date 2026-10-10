package com.nequi.bco.wks901.consumer.exchangerate;

import com.nequi.bco.wks901.model.exceptions.BusinessException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExchangeRateAdapterTest {

    private MockWebServer server;
    private ExchangeRateAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        WebClient client = WebClient.builder().baseUrl(server.url("/").toString()).build();
        adapter = new ExchangeRateAdapter(client);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void shouldReturnOneForSameCurrency() {
        StepVerifier.create(adapter.getExchangeRate("COP", "COP", "MSG-1"))
                .assertNext(rate -> assertEquals(0, BigDecimal.ONE.compareTo(rate)))
                .verifyComplete();
    }

    @Test
    void shouldReturnRateOnSuccess() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"result\":\"success\",\"base_code\":\"USD\",\"rates\":{\"COP\":4000}}"));

        StepVerifier.create(adapter.getExchangeRate("USD", "COP", "MSG-1"))
                .assertNext(rate -> assertEquals(0, new BigDecimal("4000").compareTo(rate)))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenResultNotSuccess() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"result\":\"error\",\"rates\":{}}"));

        StepVerifier.create(adapter.getExchangeRate("USD", "COP", "MSG-1"))
                .expectError(BusinessException.class)
                .verify();
    }
}

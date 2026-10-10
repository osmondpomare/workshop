package com.nequi.bco.wks901.api;

import com.nequi.bco.wks901.api.mapper.ReversalMapper;
import com.nequi.bco.wks901.model.exceptions.BusinessException;
import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.reverso.Reverso;
import com.nequi.bco.wks901.usecase.queryreversal.QueryReversalUseCase;
import com.nequi.bco.wks901.usecase.registerreversal.RegisterReversalUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReversalControllerTest {

    private RegisterReversalUseCase registerReversalUseCase;
    private QueryReversalUseCase queryReversalUseCase;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        registerReversalUseCase = mock(RegisterReversalUseCase.class);
        queryReversalUseCase = mock(QueryReversalUseCase.class);
        ReversalController controller = new ReversalController(
                registerReversalUseCase, queryReversalUseCase, new ReversalMapper());
        webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(new com.nequi.bco.wks901.api.config.GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterReversal() {
        when(registerReversalUseCase.execute(any())).thenReturn(Mono.just(Reverso.builder()
                .idReverso("R1").estado("REGISTERED").tasaCambio(new BigDecimal("4000"))
                .valorCop(new BigDecimal("400000.00")).build()));

        String body = """
                {"requestMessage":{"header":{"channel":"APP","messageId":"MSG-1","clientId":"C1",
                "destination":{"serviceName":"ReversalServices","serviceOperation":"registerReversal"}},
                "body":{"registerReversalRQ":{"transactionId":"TX-1","phoneNumber":"3001234567",
                "amount":"100.00","currency":"USD","reason":"test"}}}}""";

        webTestClient.post().uri("/services/backout/ReversalServices/registerReversal")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.responseMessage.header.status.statusCode").isEqualTo("0")
                .jsonPath("$.responseMessage.body.registerReversalRS.reversalId").isEqualTo("R1");
    }

    @Test
    void shouldReturnNotFoundEnvelopeOnQuery() {
        when(queryReversalUseCase.execute(anyString()))
                .thenReturn(Mono.error(new BusinessException(TechnicalMessage.NOT_FOUND)));

        webTestClient.get().uri("/services/backout/ReversalServices/getReversal/MSG-X")
                .header("channel", "APP")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.responseMessage.header.status.statusCode").isEqualTo("WKS901-007");
    }

    @Test
    void shouldReturnReversalOnQuery() {
        when(queryReversalUseCase.execute(anyString()))
                .thenReturn(Mono.just(Tuples.of(Reverso.builder().idReverso("R1").idMensaje("MSG-1")
                        .valor(new BigDecimal("100.00")).moneda("USD").estado("REGISTERED").build(), "NOTIFIED")));

        webTestClient.get().uri("/services/backout/ReversalServices/getReversal/MSG-1")
                .header("channel", "APP")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.responseMessage.body.getReversalRS.reversalId").isEqualTo("R1")
                .jsonPath("$.responseMessage.body.getReversalRS.eventStatus").isEqualTo("NOTIFIED");
    }
}

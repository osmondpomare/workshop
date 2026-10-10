package com.nequi.bco.wks901.usecase.queryreversal;

import com.nequi.bco.wks901.model.exceptions.BusinessException;
import com.nequi.bco.wks901.model.reverso.Reverso;
import com.nequi.bco.wks901.model.reverso.gateways.ReversalEventGateway;
import com.nequi.bco.wks901.model.reverso.gateways.ReversoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QueryReversalUseCaseTest {

    @Mock
    private ReversoRepository reversoRepository;
    @Mock
    private ReversalEventGateway reversalEventGateway;

    private QueryReversalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new QueryReversalUseCase(reversoRepository, reversalEventGateway);
    }

    @Test
    void shouldReturnReversalWithEventStatus() {
        given(reversoRepository.findByMessageId("MSG-1"))
                .willReturn(Mono.just(Reverso.builder().idReverso("R1").idMensaje("MSG-1").build()));
        given(reversalEventGateway.getEventStatus("MSG-1")).willReturn(Mono.just("NOTIFIED"));

        StepVerifier.create(useCase.execute("MSG-1"))
                .assertNext(tuple -> {
                    assertEquals("R1", tuple.getT1().getIdReverso());
                    assertEquals("NOTIFIED", tuple.getT2());
                })
                .verifyComplete();
    }

    @Test
    void shouldDefaultEventStatusToEmpty() {
        given(reversoRepository.findByMessageId("MSG-1"))
                .willReturn(Mono.just(Reverso.builder().idReverso("R1").idMensaje("MSG-1").build()));
        given(reversalEventGateway.getEventStatus("MSG-1")).willReturn(Mono.empty());

        StepVerifier.create(useCase.execute("MSG-1"))
                .assertNext(tuple -> assertEquals("", tuple.getT2()))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenNotFound() {
        given(reversoRepository.findByMessageId("MSG-X")).willReturn(Mono.empty());

        StepVerifier.create(useCase.execute("MSG-X"))
                .expectError(BusinessException.class)
                .verify();
    }
}

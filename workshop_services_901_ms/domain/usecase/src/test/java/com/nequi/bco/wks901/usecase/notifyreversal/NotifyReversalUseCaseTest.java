package com.nequi.bco.wks901.usecase.notifyreversal;

import com.nequi.bco.wks901.model.reversal.ReversalNotification;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotifyReversalUseCaseTest {

    @Mock
    private ReversoRepository reversoRepository;
    @Mock
    private ReversalEventGateway reversalEventGateway;

    private NotifyReversalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new NotifyReversalUseCase(reversoRepository, reversalEventGateway);
    }

    @Test
    void shouldNotifyReversalSuccessfully() {
        given(reversoRepository.updateEstado(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(Reverso.builder().idReverso("R1").estado("NOTIFIED").build()));
        given(reversalEventGateway.updateEventStatus("MSG-1", "NOTIFIED")).willReturn(Mono.empty());

        ReversalNotification notification = ReversalNotification.builder()
                .messageId("MSG-1").reversalId("R1").clientId("C1").channel("APP").build();

        StepVerifier.create(useCase.execute(notification))
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenUpdateFails() {
        given(reversoRepository.updateEstado(anyString(), anyString(), anyString()))
                .willReturn(Mono.error(new RuntimeException("db error")));

        ReversalNotification notification = ReversalNotification.builder()
                .messageId("MSG-1").reversalId("R1").build();

        StepVerifier.create(useCase.execute(notification))
                .expectError()
                .verify();
    }
}

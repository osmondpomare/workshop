package com.nequi.bco.wks901.usecase.registerreversal;

import com.nequi.bco.wks901.model.exceptions.BusinessException;
import com.nequi.bco.wks901.model.parametro.Parametro;
import com.nequi.bco.wks901.model.parametro.gateways.ParametroRepository;
import com.nequi.bco.wks901.model.reversal.ReversalNotification;
import com.nequi.bco.wks901.model.reversal.gateways.ReversalNotificationGateway;
import com.nequi.bco.wks901.model.reverso.ReversalRegistration;
import com.nequi.bco.wks901.model.reverso.Reverso;
import com.nequi.bco.wks901.model.reverso.gateways.ExchangeRateGateway;
import com.nequi.bco.wks901.model.reverso.gateways.ReversalEventGateway;
import com.nequi.bco.wks901.model.reverso.gateways.ReversoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RegisterReversalUseCaseTest {

    @Mock
    private ReversoRepository reversoRepository;
    @Mock
    private ParametroRepository parametroRepository;
    @Mock
    private ExchangeRateGateway exchangeRateGateway;
    @Mock
    private ReversalEventGateway reversalEventGateway;
    @Mock
    private ReversalNotificationGateway reversalNotificationGateway;

    private RegisterReversalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterReversalUseCase(reversoRepository, parametroRepository,
                exchangeRateGateway, reversalEventGateway, reversalNotificationGateway);
    }

    private ReversalRegistration validRegistration() {
        return ReversalRegistration.builder()
                .messageId("MSG-1").clientId("C1").channel("APP")
                .transactionId("TX-1").phoneNumber("3001234567")
                .amount("100.00").currency("USD").reason("test")
                .build();
    }

    @Test
    void shouldRegisterReversalSuccessfully() {
        given(reversoRepository.findByMessageId("MSG-1")).willReturn(Mono.empty());
        given(parametroRepository.getParametro(anyString()))
                .willReturn(Mono.just(Parametro.builder().idParametro("P").valor("COP").build()));
        given(exchangeRateGateway.getExchangeRate("USD", "COP", "MSG-1"))
                .willReturn(Mono.just(new BigDecimal("4000")));
        given(reversoRepository.save(any(Reverso.class)))
                .willAnswer(inv -> Mono.just(inv.getArgument(0)));
        given(reversalEventGateway.putReversalEvent(any(Reverso.class))).willReturn(Mono.empty());
        given(reversalNotificationGateway.send(any(ReversalNotification.class))).willReturn(Mono.empty());

        StepVerifier.create(useCase.execute(validRegistration()))
                .assertNext(reverso -> {
                    assertEquals("REGISTERED", reverso.getEstado());
                    assertEquals("USD", reverso.getMoneda());
                    assertEquals(0, new BigDecimal("400000.00").compareTo(reverso.getValorCop()));
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectInvalidCurrency() {
        ReversalRegistration registration = validRegistration().toBuilder().currency("US").build();

        StepVerifier.create(useCase.execute(registration))
                .expectErrorMatches(error -> error instanceof BusinessException)
                .verify();
    }

    @Test
    void shouldRejectDuplicatedReversal() {
        given(reversoRepository.findByMessageId("MSG-1"))
                .willReturn(Mono.just(Reverso.builder().idReverso("R1").build()));

        StepVerifier.create(useCase.execute(validRegistration()))
                .expectError(BusinessException.class)
                .verify();
    }
}

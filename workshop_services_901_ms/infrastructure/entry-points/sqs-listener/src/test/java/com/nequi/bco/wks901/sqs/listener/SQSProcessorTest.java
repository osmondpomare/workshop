package com.nequi.bco.wks901.sqs.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nequi.bco.wks901.model.reversal.ReversalNotification;
import com.nequi.bco.wks901.usecase.notifyreversal.NotifyReversalUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.model.Message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SQSProcessorTest {

    @Mock
    private NotifyReversalUseCase notifyReversalUseCase;

    private SQSProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new SQSProcessor(notifyReversalUseCase, new ObjectMapper());
    }

    @Test
    void shouldProcessNotificationMessage() {
        given(notifyReversalUseCase.execute(any(ReversalNotification.class))).willReturn(Mono.empty());
        Message message = Message.builder()
                .body("{\"messageId\":\"MSG-1\",\"reversalId\":\"R1\",\"clientId\":\"C1\",\"channel\":\"APP\"}")
                .build();

        StepVerifier.create(processor.apply(message)).verifyComplete();
    }

    @Test
    void shouldFailOnInvalidJson() {
        Message message = Message.builder().body("not-json").build();

        StepVerifier.create(processor.apply(message)).expectError().verify();
    }
}

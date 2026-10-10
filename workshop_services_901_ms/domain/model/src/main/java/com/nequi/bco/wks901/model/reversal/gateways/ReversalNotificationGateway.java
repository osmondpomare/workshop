package com.nequi.bco.wks901.model.reversal.gateways;

import com.nequi.bco.wks901.model.reversal.ReversalNotification;
import reactor.core.publisher.Mono;

/**
 * Publishes a reversal notification to the asynchronous messaging channel.
 * Replaces the legacy GenericSendJMSMesssageBean JMS send.
 */
public interface ReversalNotificationGateway {

    Mono<Void> send(ReversalNotification notification);
}

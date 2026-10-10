package com.nequi.bco.wks901.config;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.UUID;

/**
 * Reactive WebFilter that extracts correlation headers and propagates them via MDC
 * (current thread) and Reactor Context (async pipeline).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationWebFilter implements WebFilter {

    private static final String MESSAGE_ID = "messageId";
    private static final String CHANNEL = "channel";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String messageId = Optional.ofNullable(
                        exchange.getRequest().getHeaders().getFirst("x-message-id"))
                .orElse(UUID.randomUUID().toString());
        String channel = Optional.ofNullable(
                        exchange.getRequest().getHeaders().getFirst("channel"))
                .orElse("unknown");

        MDC.put(MESSAGE_ID, messageId);
        MDC.put(CHANNEL, channel);

        return chain.filter(exchange)
                .contextWrite(Context.of(MESSAGE_ID, messageId, CHANNEL, channel))
                .doFinally(signalType -> MDC.clear());
    }
}

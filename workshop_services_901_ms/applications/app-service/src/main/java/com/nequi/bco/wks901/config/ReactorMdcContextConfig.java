package com.nequi.bco.wks901.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

/**
 * Propagates Reactor Context values into SLF4J MDC across operator thread switches,
 * so correlation data (messageId, channel) appears in structured JSON logs.
 */
@Configuration
public class ReactorMdcContextConfig {

    private static final String MDC_CONTEXT_REACTOR_KEY = "reactor.mdc";

    @PostConstruct
    public void configureMdcContextPropagation() {
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY,
                Operators.lift((scannable, subscriber) -> new MdcContextLifter<>(subscriber)));
    }

    @PreDestroy
    public void cleanupHook() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY);
    }
}

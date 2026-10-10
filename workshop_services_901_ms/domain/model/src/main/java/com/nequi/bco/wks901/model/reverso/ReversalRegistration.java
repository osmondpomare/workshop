package com.nequi.bco.wks901.model.reverso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Typed input for the register reversal flow, assembled by the entry point
 * from the transport request (header + body).
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReversalRegistration {

    String messageId;
    String clientId;
    String channel;
    String transactionId;
    String phoneNumber;
    String amount;
    String currency;
    String reason;
}

package com.nequi.bco.wks901.model.reversal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Business event emitted after a reversal is registered and consumed asynchronously
 * to confirm the reversal notification.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReversalNotification {

    String messageId;
    String reversalId;
    String clientId;
    String channel;
}

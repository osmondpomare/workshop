package com.nequi.bco.wks901.api.mapper;

import com.nequi.bco.wks901.api.dto.MessageRQ;
import com.nequi.bco.wks901.api.dto.MessageRS;
import com.nequi.bco.wks901.api.dto.RegisterReversalRQType;
import com.nequi.bco.wks901.api.dto.RequestBodyType;
import com.nequi.bco.wks901.api.dto.RequestHeaderType;
import com.nequi.bco.wks901.api.dto.RequestMessageType;
import com.nequi.bco.wks901.model.reverso.ReversalRegistration;
import com.nequi.bco.wks901.model.reverso.Reverso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReversalMapperTest {

    private final ReversalMapper mapper = new ReversalMapper();

    @Test
    void shouldMapRequestToRegistration() {
        MessageRQ rq = new MessageRQ(new RequestMessageType(
                new RequestHeaderType("APP", "MSG-1", "C1", null),
                new RequestBodyType(new RegisterReversalRQType("TX-1", "3001234567", "100.00", "USD", "r"))));

        ReversalRegistration registration = mapper.toRegistration(rq);

        assertEquals("MSG-1", registration.getMessageId());
        assertEquals("USD", registration.getCurrency());
        assertEquals("TX-1", registration.getTransactionId());
    }

    @Test
    void shouldBuildRegisterResponse() {
        ReversalRegistration registration = ReversalRegistration.builder()
                .channel("APP").messageId("MSG-1").clientId("C1").build();
        Reverso reverso = Reverso.builder().idReverso("R1").estado("REGISTERED")
                .tasaCambio(new BigDecimal("4000")).valorCop(new BigDecimal("400000.00")).build();

        MessageRS rs = mapper.buildRegisterResponse(registration, reverso);

        assertEquals("0", rs.responseMessage().header().status().statusCode());
        assertEquals("R1", rs.responseMessage().body().registerReversalRS().reversalId());
        assertNotNull(rs.responseMessage().header().responseDate());
    }
}

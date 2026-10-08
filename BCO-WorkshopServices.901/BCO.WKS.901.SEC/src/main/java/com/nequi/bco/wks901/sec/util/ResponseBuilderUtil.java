package com.nequi.bco.wks901.sec.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.nequi.bco.wks901.mel.services.reversal.MessageRS;
import com.nequi.bco.wks901.mel.services.reversal.RequestHeaderType;
import com.nequi.bco.wks901.mel.services.reversal.ResponseBodyType;
import com.nequi.bco.wks901.mel.services.reversal.ResponseHeaderType;
import com.nequi.bco.wks901.mel.services.reversal.ResponseMessageType;
import com.nequi.bco.wks901.mel.services.reversal.StatusType;

public final class ResponseBuilderUtil {

    private ResponseBuilderUtil() {
    }

    public static MessageRS buildResponse(RequestHeaderType requestHeader, String statusCode, String statusDesc,
            ResponseBodyType body) {
        String channel = null;
        String messageId = null;
        String clientId = null;
        if (null != requestHeader) {
            channel = requestHeader.getChannel();
            messageId = requestHeader.getMessageId();
            clientId = requestHeader.getClientId();
        }
        return buildResponse(channel, messageId, clientId, statusCode, statusDesc, body);
    }

    public static MessageRS buildResponse(String channel, String messageId, String clientId, String statusCode,
            String statusDesc, ResponseBodyType body) {
        StatusType status = new StatusType();
        status.setStatusCode(statusCode);
        status.setStatusDesc(statusDesc);

        ResponseHeaderType header = new ResponseHeaderType();
        header.setChannel(channel);
        header.setMessageId(messageId);
        header.setClientId(clientId);
        header.setResponseDate(formatDate(new Date()));
        header.setStatus(status);

        ResponseMessageType responseMessage = new ResponseMessageType();
        responseMessage.setHeader(header);
        responseMessage.setBody(body);

        MessageRS messageRS = new MessageRS();
        messageRS.setResponseMessage(responseMessage);
        return messageRS;
    }

    public static String formatDate(Date date) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(ConstantWKS901.DATE_PATTERN);
        format.setTimeZone(TimeZone.getTimeZone(ConstantWKS901.TIME_ZONE));
        return format.format(date);
    }
}

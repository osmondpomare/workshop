package org.bintec.bco.wks901.sef.service;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bintec.bco.wks901.mel.services.reversal.MessageRQ;
import org.bintec.bco.wks901.mel.services.reversal.MessageRS;
import org.bintec.bco.wks901.mel.services.reversal.RequestHeaderType;
import org.bintec.bco.wks901.sec.bean.reversal.QueryReversalBean;
import org.bintec.bco.wks901.sec.bean.reversal.RegisterReversalBean;
import org.bintec.bco.wks901.sec.util.ConstantWKS901;
import org.bintec.bco.wks901.sec.util.MessageTracerUtil;
import org.bintec.bco.wks901.sec.util.ResponseBuilderUtil;
import org.bintec.mdw.common.consumer.exception.CommonUtilException;
import org.bintec.mdw.common.consumer.util.UtilJSON;
import org.bintec.mdw.common.tracer.service.GenericLogger;
import org.bintec.mdw.common.tracer.service.LoggerFactory;
import org.bintec.mdw.common.tracer.service.ServiceTypeEnum;

@Path("/services/backout/ReversalServices")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class ReversalService {

    private static final String ERROR_RESPONSE = "{\"responseMessage\":{\"header\":{\"status\":{\"statusCode\":\""
            + ConstantWKS901.ERROR_CODE_UNEXPECTED + "\",\"statusDesc\":\"" + ConstantWKS901.ERROR_DESC_UNEXPECTED
            + "\"}}}}";

    private final GenericLogger logger;

    @EJB
    private RegisterReversalBean registerReversalBean;

    @EJB
    private QueryReversalBean queryReversalBean;

    public ReversalService() {
        logger = LoggerFactory.getLogger(ServiceTypeEnum.FACADE, ReversalService.class);
    }

    @POST
    @Path("/registerReversal")
    public String registerReversal(String request) {
        MessageRS messageRS;
        MessageRQ messageRQ = null;
        try {
            messageRQ = UtilJSON.parsePayload(request, MessageRQ.class);
        } catch (CommonUtilException e) {
            logger.traceError(ConstantWKS901.ERROR_DESC_INVALID_REQUEST, e);
        }

        if (null == messageRQ || null == messageRQ.getRequestMessage()) {
            messageRS = ResponseBuilderUtil.buildResponse((RequestHeaderType) null,
                    ConstantWKS901.ERROR_CODE_INVALID_REQUEST, ConstantWKS901.ERROR_DESC_INVALID_REQUEST, null);
        } else {
            MessageTracerUtil.traceRequestMessage(messageRQ.getRequestMessage().getHeader(),
                    messageRQ.getRequestMessage().getBody(), logger);
            messageRS = registerReversalBean.registerReversal(messageRQ);
        }
        return toResponse(messageRS);
    }

    @GET
    @Path("/getReversal/{messageId}")
    public String getReversal(@PathParam("messageId") String messageId, @HeaderParam("channel") String channel) {
        MessageTracerUtil.traceRequestMessage(null, "{\"messageId\":\"" + messageId + "\"}", logger);
        return toResponse(queryReversalBean.getReversal(messageId, channel));
    }

    private String toResponse(MessageRS messageRS) {
        MessageTracerUtil.traceResponseMessage(messageRS.getResponseMessage().getHeader(),
                messageRS.getResponseMessage().getBody(), logger);
        try {
            return UtilJSON.parseObjectToString(messageRS);
        } catch (CommonUtilException e) {
            logger.traceError(ConstantWKS901.TRACE_PARSE_FAILED, e);
            return ERROR_RESPONSE;
        }
    }
}

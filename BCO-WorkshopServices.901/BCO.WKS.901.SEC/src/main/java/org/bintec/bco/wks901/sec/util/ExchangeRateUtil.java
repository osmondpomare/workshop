package org.bintec.bco.wks901.sec.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.bintec.bco.wks901.sec.exception.WKS901Exception;
import org.bintec.mdw.common.consumer.broker.ServiceBroker;
import org.bintec.mdw.common.consumer.exception.CommonUtilException;
import org.bintec.mdw.common.consumer.exception.ServiceConsumerException;
import org.bintec.mdw.common.consumer.util.UtilJSON;
import org.bintec.mdw.common.tracer.service.GenericLogger;

public final class ExchangeRateUtil {

    private ExchangeRateUtil() {
    }

    public static BigDecimal getExchangeRate(String sourceCurrency, String targetCurrency, String messageId,
            GenericLogger logger) throws WKS901Exception {

        if (sourceCurrency.equalsIgnoreCase(targetCurrency)) {
            return BigDecimal.ONE;
        }

        Map<String, String> pathParams = new HashMap<String, String>();
        pathParams.put(ConstantWKS901.PATH_PARAM_CURRENCY, sourceCurrency);

        try {
            String response = ServiceBroker.invoke(ConstantWKS901.SERVICE_EXCHANGE_RATE,
                    ConstantWKS901.OPERATION_GET_LATEST_RATES, pathParams, null, null, messageId, logger);
            JsonNode tree = UtilJSON.parseToTree(response);
            JsonNode result = tree.get(ConstantWKS901.EXCHANGE_RESULT_FIELD);
            JsonNode rates = tree.get(ConstantWKS901.EXCHANGE_RATES_FIELD);
            if (null == result || !ConstantWKS901.EXCHANGE_RESULT_SUCCESS.equals(result.asText()) || null == rates
                    || null == rates.get(targetCurrency)) {
                throw new WKS901Exception(ConstantWKS901.ERROR_CODE_EXCHANGE_SERVICE,
                        ConstantWKS901.ERROR_DESC_EXCHANGE_SERVICE);
            }
            BigDecimal rate = rates.get(targetCurrency).decimalValue();
            logger.traceInfo(UtilJSON.generateString(ConstantWKS901.TRACE_EXCHANGE_RATE, sourceCurrency, "/",
                    targetCurrency, "=", rate.toPlainString()));
            return rate;
        } catch (ServiceConsumerException e) {
            throw new WKS901Exception(ConstantWKS901.ERROR_CODE_EXCHANGE_SERVICE,
                    ConstantWKS901.ERROR_DESC_EXCHANGE_SERVICE, e);
        } catch (CommonUtilException e) {
            throw new WKS901Exception(ConstantWKS901.ERROR_CODE_EXCHANGE_SERVICE,
                    ConstantWKS901.ERROR_DESC_EXCHANGE_SERVICE, e);
        }
    }
}

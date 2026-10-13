package org.bintec.bco.wks901.sec.util;

public final class ConstantWKS901 {

    public static final String PERSISTENCE_UNIT = "JPAManager";

    public static final String COMMON_STRING_ENVIROMENT = "java:comp/env";
    public static final String COMMON_STRING_QUEUE_FACTORY = "jms/WKS-CF";
    public static final String COMMON_STRING_QUEUE_NOTIFY_REVERSAL = "jms/BCO-NotifyReversal.Q";
    public static final String COMMON_STRING_JMSX_DELIVERY_COUNT = "JMSXDeliveryCount";
    public static final String COMMON_STRING_GENERIC_JMS_MESSAGE = "GenericSendJMSMesssageBean";

    public static final String SERVICE_NAME = "ReversalServices";
    public static final String SERVICE_OPERATION_REGISTER = "registerReversal";
    public static final String SERVICE_OPERATION_GET = "getReversal";
    public static final String SERVICE_OPERATION_NOTIFY = "notifyReversal";

    public static final String SERVICE_EXCHANGE_RATE = "ExchangeRateServices";
    public static final String OPERATION_GET_LATEST_RATES = "getLatestRates";
    public static final String PATH_PARAM_CURRENCY = "currency";
    public static final String EXCHANGE_RESULT_FIELD = "result";
    public static final String EXCHANGE_RESULT_SUCCESS = "success";
    public static final String EXCHANGE_RATES_FIELD = "rates";

    public static final String PARAM_TARGET_CURRENCY = "BCO_WKS901_MONEDA_DESTINO";
    public static final String PARAM_MAX_REDELIVERY = "BCO_WKS901_MAX_REINTENTOS";
    public static final String DEFAULT_TARGET_CURRENCY = "COP";
    public static final int DEFAULT_MAX_REDELIVERY = 3;

    public static final String STATUS_REGISTERED = "REGISTERED";
    public static final String STATUS_NOTIFIED = "NOTIFIED";
    public static final String STATUS_FAILED = "FAILED";

    public static final String CURRENT_USER_ENV = "CURRENT_USER";
    public static final String DEFAULT_USER = "Nequi";

    public static final String DYNAMO_ENDPOINT_ENV = "DYNAMO_ENDPOINT";
    public static final String DYNAMO_REGION_ENV = "AWS_REGION";
    public static final String DYNAMO_ACCESS_KEY_ENV = "AWS_ACCESS_KEY_ID";
    public static final String DYNAMO_SECRET_KEY_ENV = "AWS_SECRET_ACCESS_KEY";
    public static final String DYNAMO_TABLE_ENV = "DYNAMO_REVERSAL_TABLE";
    public static final String DYNAMO_DEFAULT_REGION = "us-east-1";
    public static final String DYNAMO_DEFAULT_TABLE = "BCO_REVERSAL_EVENTS";
    public static final String DYNAMO_ATTR_MESSAGE_ID = "messageId";
    public static final String DYNAMO_ATTR_REVERSAL_ID = "reversalId";
    public static final String DYNAMO_ATTR_TRANSACTION_ID = "transactionId";
    public static final String DYNAMO_ATTR_PHONE_NUMBER = "phoneNumber";
    public static final String DYNAMO_ATTR_AMOUNT = "amount";
    public static final String DYNAMO_ATTR_CURRENCY = "currency";
    public static final String DYNAMO_ATTR_AMOUNT_COP = "amountCop";
    public static final String DYNAMO_ATTR_EVENT_STATUS = "eventStatus";
    public static final String DYNAMO_ATTR_EVENT_DATE = "eventDate";
    public static final String DYNAMO_ATTR_NOTIFIED_DATE = "notifiedDate";
    public static final String DYNAMO_CONDITION_NEW_ITEM = "attribute_not_exists(messageId)";
    public static final String DYNAMO_UPDATE_STATUS = "SET eventStatus = :status, notifiedDate = :notifiedDate";
    public static final String DYNAMO_VALUE_STATUS = ":status";
    public static final String DYNAMO_VALUE_NOTIFIED_DATE = ":notifiedDate";

    public static final String SUCCESS_CODE = "0";
    public static final String SUCCESS_DESC = "SUCCESS";
    public static final String ERROR_CODE_INVALID_REQUEST = "WKS901-001";
    public static final String ERROR_CODE_DUPLICATED = "WKS901-002";
    public static final String ERROR_CODE_EXCHANGE_SERVICE = "WKS901-003";
    public static final String ERROR_CODE_DATABASE = "WKS901-004";
    public static final String ERROR_CODE_DYNAMO = "WKS901-005";
    public static final String ERROR_CODE_JMS = "WKS901-006";
    public static final String ERROR_CODE_NOT_FOUND = "WKS901-007";
    public static final String ERROR_CODE_UNEXPECTED = "WKS901-999";

    public static final String ERROR_DESC_INVALID_REQUEST = "Invalid request message";
    public static final String ERROR_DESC_DUPLICATED = "A reversal already exists for this messageId";
    public static final String ERROR_DESC_EXCHANGE_SERVICE = "Unable to obtain exchange rate";
    public static final String ERROR_DESC_DATABASE = "Database error";
    public static final String ERROR_DESC_DYNAMO = "Error registering reversal event";
    public static final String ERROR_DESC_JMS = "Error sending reversal notification";
    public static final String ERROR_DESC_NOT_FOUND = "Reversal not found";
    public static final String ERROR_DESC_UNEXPECTED = "Unexpected error";

    public static final String TRACE_MDB_START = "MdbNotifyReversalBean start onMessage";
    public static final String TRACE_MDB_END = "MdbNotifyReversalBean end onMessage";
    public static final String TRACE_MDB_ATTEMPT = "JMS redelivery attempt number: ";
    public static final String TRACE_MDB_MAX_ATTEMPTS = "Max redelivery attempts reached, message discarded: ";
    public static final String TRACE_MDB_INVALID_MESSAGE = "Unsupported JMS message type";
    public static final String TRACE_NOTIFIED = "Reversal notified: ";
    public static final String TRACE_EXCHANGE_RATE = "Exchange rate obtained: ";
    public static final String TRACE_PARSE_FAILED = "Unable to serialize message for trace";

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String TIME_ZONE = "America/Bogota";
    public static final String CURRENCY_PATTERN = "^[A-Z]{3}$";
    public static final String COMMON_STRING_EMPTY = "";
    public static final int AMOUNT_SCALE = 2;
    public static final int RATE_SCALE = 6;

    private ConstantWKS901() {
    }
}

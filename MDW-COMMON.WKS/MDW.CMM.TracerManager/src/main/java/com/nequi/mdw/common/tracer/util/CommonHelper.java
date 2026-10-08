package com.nequi.mdw.common.tracer.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

public final class CommonHelper {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private CommonHelper() {
    }

    public static String currentDate() {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        format.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
        return format.format(new Date());
    }

    public static String stackTrace(Throwable ex) {
        if (null == ex) {
            return null;
        }
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    public static Map<String, Object> newEntry() {
        return new LinkedHashMap<String, Object>();
    }

    public static String toJson(Map<String, Object> values) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (null == entry.getValue()) {
                continue;
            }
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value instanceof RawJson) {
                builder.append(((RawJson) value).getValue());
            } else if (value instanceof Number || value instanceof Boolean) {
                builder.append(value.toString());
            } else {
                builder.append('"').append(escape(String.valueOf(value))).append('"');
            }
        }
        return builder.append('}').toString();
    }

    public static Object jsonOrText(String value) {
        if (null == value) {
            return null;
        }
        String trimmed = value.trim();
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return new RawJson(trimmed);
        }
        return value;
    }

    public static String escape(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
            case '"':
                builder.append("\\\"");
                break;
            case '\\':
                builder.append("\\\\");
                break;
            case '\n':
                builder.append("\\n");
                break;
            case '\r':
                builder.append("\\r");
                break;
            case '\t':
                builder.append("\\t");
                break;
            default:
                if (c < 0x20) {
                    builder.append(String.format("\\u%04x", (int) c));
                } else {
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    public static final class RawJson {

        private final String value;

        public RawJson(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

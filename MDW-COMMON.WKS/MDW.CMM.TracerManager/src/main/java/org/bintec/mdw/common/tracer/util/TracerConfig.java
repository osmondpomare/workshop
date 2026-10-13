package org.bintec.mdw.common.tracer.util;

public final class TracerConfig {

    public static final String DEBUG_PROPERTY = "tracer.debug.enabled";
    public static final String DEBUG_ENV = "TRACER_DEBUG_ENABLED";
    public static final String TRACEABLE_ENV = "TRACER_TRACEABLE";

    private TracerConfig() {
    }

    public static boolean isDebugEnabled() {
        return readFlag(DEBUG_PROPERTY, DEBUG_ENV, false);
    }

    public static boolean isTraceable() {
        return readFlag("tracer.traceable", TRACEABLE_ENV, true);
    }

    private static boolean readFlag(String property, String env, boolean defaultValue) {
        String value = System.getProperty(property);
        if (null == value || value.trim().isEmpty()) {
            value = System.getenv(env);
        }
        if (null == value || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }
}

package com.sme.erp.audit.service;

public final class ActivityLogInvocationContext {
    private static final ThreadLocal<Boolean> LOGGED = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private ActivityLogInvocationContext() {
    }

    public static void reset() {
        LOGGED.set(Boolean.FALSE);
    }

    public static void markLogged() {
        LOGGED.set(Boolean.TRUE);
    }

    public static boolean wasLogged() {
        return LOGGED.get();
    }

    public static void clear() {
        LOGGED.remove();
    }
}

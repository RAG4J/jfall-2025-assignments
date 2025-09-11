package org.rag4j.agent.core;

public final class RequestIdentity {
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private RequestIdentity() {}
    public static void set(String userId) { USER_ID.set(userId); }
    public static String getUserId() { return USER_ID.get(); }
    public static void clear() { USER_ID.remove(); }
}

package org.datpham.foodlink.constant;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    // TODO: Centralize security-related constants here.
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_SECRET_ENV = "JWT_SECRET";
    public static final long JWT_EXPIRATION_MS = 24 * 60 * 60 * 1000L;
}

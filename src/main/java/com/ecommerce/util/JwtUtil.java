package com.ecommerce.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = System.getenv().getOrDefault(
            "JWT_SECRET", "change-me-in-production");

    // Access token: short-lived (15 minutes)
    private static final long ACCESS_EXPIRATION_MS = 15 * 60 * 1000L;

    // Refresh token: long-lived (7 days)
    private static final long REFRESH_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    /**
     * Generate an access token (short-lived, 15 min).
     */
    public static String generateAccessToken(Long userId, String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_MS))
                .sign(ALGORITHM);
    }

    /**
     * Generate a refresh token (long-lived, 7 days).
     */
    public static String generateRefreshToken(Long userId, String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withClaim("type", "refresh")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_MS))
                .sign(ALGORITHM);
    }

    /**
     * @deprecated Use {@link #generateAccessToken} instead.
     */
    @Deprecated
    public static String generate(Long userId, String email, String role) {
        return generateAccessToken(userId, email, role);
    }

    public static DecodedJWT verify(String token) {
        return JWT.require(ALGORITHM).build().verify(token);
    }

    public static boolean isRefreshToken(DecodedJWT jwt) {
        return "refresh".equals(jwt.getClaim("type").asString());
    }

    public static boolean isAccessToken(DecodedJWT jwt) {
        String type = jwt.getClaim("type").asString();
        return type == null || "access".equals(type);
    }

    public static int getAccessTokenMaxAgeSec() {
        return (int) (ACCESS_EXPIRATION_MS / 1000);
    }

    public static int getRefreshTokenMaxAgeSec() {
        return (int) (REFRESH_EXPIRATION_MS / 1000);
    }

    public static long getExpirationSeconds() {
        return ACCESS_EXPIRATION_MS / 1000;
    }
}

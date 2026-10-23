package com.ecommerce.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility for managing JWT cookies (HttpOnly, not accessible via JavaScript).
 */
public class CookieUtil {

    public static final String ACCESS_COOKIE  = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";

    private static final boolean SECURE = Boolean.parseBoolean(
            System.getenv().getOrDefault("COOKIE_SECURE", "false"));

    // -------------------------------------------------------------------------
    // Add cookies
    // -------------------------------------------------------------------------

    /**
     * Set both access and refresh token cookies on the response.
     */
    public static void addTokenCookies(HttpServletResponse res,
                                       String accessToken,
                                       String refreshToken) {
        addCookie(res, ACCESS_COOKIE,  accessToken,  JwtUtil.getAccessTokenMaxAgeSec());
        addCookie(res, REFRESH_COOKIE, refreshToken, JwtUtil.getRefreshTokenMaxAgeSec());
    }

    // -------------------------------------------------------------------------
    // Read cookies
    // -------------------------------------------------------------------------

    /**
     * Extract a cookie value by name from the request.
     * Returns null if not found.
     */
    public static String getCookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    public static String getAccessToken(HttpServletRequest req) {
        return getCookieValue(req, ACCESS_COOKIE);
    }

    public static String getRefreshToken(HttpServletRequest req) {
        return getCookieValue(req, REFRESH_COOKIE);
    }

    // -------------------------------------------------------------------------
    // Clear cookies (logout)
    // -------------------------------------------------------------------------

    /**
     * Remove both token cookies by setting maxAge to 0.
     */
    public static void clearTokenCookies(HttpServletResponse res) {
        clearCookie(res, ACCESS_COOKIE);
        clearCookie(res, REFRESH_COOKIE);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static void addCookie(HttpServletResponse res, String name,
                                  String value, int maxAgeSec) {
        // Use Set-Cookie header directly to support SameSite attribute
        String header = String.format(
                "%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax%s",
                name, value, maxAgeSec, SECURE ? "; Secure" : "");
        res.addHeader("Set-Cookie", header);
    }

    private static void clearCookie(HttpServletResponse res, String name) {
        String header = String.format(
                "%s=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax%s",
                name, SECURE ? "; Secure" : "");
        res.addHeader("Set-Cookie", header);
    }
}

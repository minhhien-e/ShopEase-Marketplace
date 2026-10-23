package com.ecommerce.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.util.CookieUtil;
import com.ecommerce.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/api/*")
public class JwtFilter implements Filter {

    private static final String[] PUBLIC_ROUTES = {
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/refresh",
            "/api/auth/logout"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // CORS — credentials: true requires a specific origin, not "*"
        String origin = req.getHeader("Origin");
        if (origin != null) {
            res.setHeader("Access-Control-Allow-Origin", origin);
        }
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type");
        res.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = req.getServletPath();
        String method = req.getMethod();

        // Public routes (login, signup, refresh, logout)
        for (String pub : PUBLIC_ROUTES) {
            if (path.equals(pub)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // GET /api/products — public, but optionally extract user from cookie
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/products")) {
            tryExtractUserFromCookie(req, res);
            chain.doFilter(request, response);
            return;
        }

        // All other routes require a valid access token in cookie
        String accessToken = CookieUtil.getAccessToken(req);

        if (accessToken == null || accessToken.isEmpty()) {
            // No access token — try to auto-refresh using refresh token
            if (tryAutoRefresh(req, res)) {
                chain.doFilter(request, response);
            } else {
                sendError(res, 401, "Authentication required. Please log in.");
            }
            return;
        }

        try {
            DecodedJWT jwt = JwtUtil.verify(accessToken);

            // Reject refresh tokens used as access tokens
            if (JwtUtil.isRefreshToken(jwt)) {
                sendError(res, 401, "Invalid token type.");
                return;
            }

            req.setAttribute("userId", jwt.getClaim("userId").asLong());
            req.setAttribute("email", jwt.getSubject());
            req.setAttribute("role", jwt.getClaim("role").asString());
            chain.doFilter(request, response);

        } catch (TokenExpiredException e) {
            // Access token expired — try auto-refresh
            if (tryAutoRefresh(req, res)) {
                chain.doFilter(request, response);
            } else {
                sendError(res, 401, "Session expired. Please log in again.");
            }

        } catch (JWTVerificationException e) {
            sendError(res, 401, "Invalid token.");
        }
    }

    /**
     * Try to extract user info from cookie without blocking the request.
     * Used for optional auth on public endpoints.
     */
    private void tryExtractUserFromCookie(HttpServletRequest req, HttpServletResponse res) {
        String accessToken = CookieUtil.getAccessToken(req);
        if (accessToken == null || accessToken.isEmpty()) return;

        try {
            DecodedJWT jwt = JwtUtil.verify(accessToken);
            if (JwtUtil.isAccessToken(jwt)) {
                req.setAttribute("userId", jwt.getClaim("userId").asLong());
                req.setAttribute("email", jwt.getSubject());
                req.setAttribute("role", jwt.getClaim("role").asString());
            }
        } catch (JWTVerificationException e) {
            // Token invalid — ignore, request continues as anonymous
        }
    }

    /**
     * Attempt to refresh the access token using the refresh token cookie.
     * If successful, sets new cookies and populates request attributes.
     */
    private boolean tryAutoRefresh(HttpServletRequest req, HttpServletResponse res) {
        String refreshToken = CookieUtil.getRefreshToken(req);
        if (refreshToken == null || refreshToken.isEmpty()) return false;

        try {
            DecodedJWT jwt = JwtUtil.verify(refreshToken);
            if (!JwtUtil.isRefreshToken(jwt)) return false;

            Long userId = jwt.getClaim("userId").asLong();
            String email = jwt.getSubject();
            String role = jwt.getClaim("role").asString();

            // Generate new access token (keep the same refresh token)
            String newAccessToken = JwtUtil.generateAccessToken(userId, email, role);
            CookieUtil.addTokenCookies(res, newAccessToken, refreshToken);

            // Set request attributes for this request
            req.setAttribute("userId", userId);
            req.setAttribute("email", email);
            req.setAttribute("role", role);
            return true;

        } catch (JWTVerificationException e) {
            // Refresh token also invalid/expired — must re-login
            CookieUtil.clearTokenCookies(res);
            return false;
        }
    }

    private void sendError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}

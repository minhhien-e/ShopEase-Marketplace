package com.ecommerce.servlet;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.util.CookieUtil;
import com.ecommerce.util.JwtUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Endpoint to refresh the access token using a valid refresh token cookie.
 * POST /api/auth/refresh — no body required, reads refresh_token from cookie.
 */
@WebServlet("/api/auth/refresh")
public class RefreshServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String refreshToken = CookieUtil.getRefreshToken(req);

        if (refreshToken == null || refreshToken.isEmpty()) {
            sendError(res, 401, "No refresh token found. Please log in.");
            return;
        }

        try {
            DecodedJWT jwt = JwtUtil.verify(refreshToken);

            if (!JwtUtil.isRefreshToken(jwt)) {
                sendError(res, 401, "Invalid token type.");
                return;
            }

            Long userId = jwt.getClaim("userId").asLong();
            String email = jwt.getSubject();
            String role = jwt.getClaim("role").asString();

            // Generate new access token, keep same refresh token
            String newAccessToken = JwtUtil.generateAccessToken(userId, email, role);
            CookieUtil.addTokenCookies(res, newAccessToken, refreshToken);

            sendJson(res, 200, Map.of("message", "Token refreshed successfully"));

        } catch (JWTVerificationException e) {
            CookieUtil.clearTokenCookies(res);
            sendError(res, 401, "Refresh token expired. Please log in again.");
        }
    }
}

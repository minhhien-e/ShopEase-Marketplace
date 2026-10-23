package com.ecommerce.servlet;

import com.ecommerce.entity.User;
import com.ecommerce.service.AuthService;
import com.ecommerce.util.CookieUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/auth/login")
public class LoginServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        Map<String, String> body = readBody(req);

        String email = body.get("email");
        String password = body.get("password");

        if (isBlank(email) || isBlank(password)) {
            sendError(res, 400, "email and password are required");
            return;
        }

        try {
            // LOGIN — returns user info only (no token in body)
            Map<String, Object> response = authService.login(email, password);

            // Generate tokens and set as HttpOnly cookies
            User user = authService.findByEmail(email);
            Map<String, String> tokens = authService.generateTokens(user);
            CookieUtil.addTokenCookies(res, tokens.get("accessToken"), tokens.get("refreshToken"));

            // Response body contains ONLY user info — NO tokens exposed
            sendJson(res, 200, response);
            MetricsServlet.loginSuccessTotal.incrementAndGet();

        } catch (SecurityException e) {
            MetricsServlet.loginFailureTotal.incrementAndGet();
            sendError(res, 401, e.getMessage());

        } catch (Exception e) {
            MetricsServlet.loginFailureTotal.incrementAndGet();
            sendError(res, 500, "An unexpected error occurred");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

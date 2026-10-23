package com.ecommerce.servlet;

import com.ecommerce.util.CookieUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/auth/logout")
public class LogoutServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Clear both access and refresh token cookies
        CookieUtil.clearTokenCookies(res);
        sendJson(res, 200, Map.of("message", "Logged out successfully"));
    }
}

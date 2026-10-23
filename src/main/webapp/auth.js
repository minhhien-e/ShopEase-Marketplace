/**
 * Global fetch interceptor — automatically adds credentials: 'include'
 * to all fetch calls so cookies (HttpOnly JWT tokens) are always sent.
 * This eliminates the need to add credentials to each individual fetch call.
 */
(function() {
    const originalFetch = window.fetch;
    window.fetch = function(url, options = {}) {
        // Always include credentials so HttpOnly cookies are sent
        if (!options.credentials) {
            options.credentials = 'include';
        }
        return originalFetch.call(this, url, options);
    };
})();
/**
 * auth.js — Drop this ONE <script> tag into any page's <head>.
 * It auto-applies the right rule based on the current page.
 *
 * Tokens are now stored in HttpOnly cookies (not accessible via JS).
 * Authentication state is determined by the 'user' key in localStorage
 * (which contains user info only, NOT tokens).
 *
 * All fetch() calls MUST include { credentials: 'include' } to send cookies.
 */

const Auth = (() => {

    // Storage keys — only user info, NO tokens
    const USER_KEY     = 'user';
    const INTENDED_KEY = 'auth_intended';

    // Route map
    const ROUTE_MAP = {
        'login.html'     : 'guest',
        'cart.html'      : 'auth',
        'signup.html'    : 'guest',
        'index.html'     : ['guest', 'auth'],
        'dashboard.html' : ['SELLER', 'ADMIN'],
        'profile.html'   : 'auth',
        'payment.html'   : 'auth',
        'products.html'  : ['ADMIN', 'SELLER'],
        'product.html'   : 'auth',
        'store.html'     : 'auth',
        'admin.html'     : ['ADMIN'],
    };

    // Helpers

    function getUser() {
        try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); }
        catch { return null; }
    }

    function isLoggedIn() { return !!getUser(); }

    function currentPage() {
        const parts = window.location.pathname.split('/');
        return parts[parts.length - 1] || 'index.html';
    }

    function saveIntended() {
        sessionStorage.setItem(INTENDED_KEY, window.location.href);
    }

    function popIntended(fallback = 'index.html') {
        const url = sessionStorage.getItem(INTENDED_KEY) || fallback;
        sessionStorage.removeItem(INTENDED_KEY);
        return url;
    }

    /**
     * Wrapper around fetch that automatically includes credentials (cookies)
     * and handles 401 responses by attempting a token refresh.
     */
    async function authFetch(url, options = {}) {
        options.credentials = 'include';

        let res = await fetch(url, options);

        // If 401, try to refresh the token and retry once
        if (res.status === 401) {
            const refreshRes = await fetch('/api/auth/refresh', {
                method: 'POST',
                credentials: 'include'
            });

            if (refreshRes.ok) {
                // Retry the original request with new cookie
                res = await fetch(url, options);
            } else {
                // Refresh failed — session expired, redirect to login
                localStorage.removeItem(USER_KEY);
                saveIntended();
                window.location.replace('login.html');
                return res;
            }
        }

        return res;
    }

    /**
     * Logout — call the server to clear cookies, then clear local state.
     */
    async function logout() {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                credentials: 'include'
            });
        } catch (e) {
            // Ignore network errors during logout
        }
        localStorage.removeItem(USER_KEY);
        window.location.replace('login.html');
    }

    // Guards

    function requireAuth() {
        if (!isLoggedIn()) {
            saveIntended();
            window.location.replace('login.html');
            return null;
        }
        return getUser();
    }

    function requireGuest() {
        if (isLoggedIn()) {
            const previousPage = document.referrer;
            if (previousPage && !previousPage.includes('login.html') && !previousPage.includes('signup.html')) {
                window.location.replace(previousPage);
            } else {
                window.location.replace('store.html');
            }
        }
    }

    function requireRole(...roles) {
        const user = requireAuth();
        if (!user) return null;

        if (!roles.includes(user.role)) {
            window.location.replace('403.html');
            return null;
        }
        return user;
    }

    function redirectAfterLogin() {
        const user = getUser();
        if (user?.role === 'ADMIN')  { window.location.replace('admin.html');    return; }
        if (user?.role === 'SELLER') { window.location.replace('dashboard.html'); return; }
        window.location.replace('store.html');
    }

    // Auto-run based on ROUTE_MAP

    (function autoGuard() {
        const page = currentPage();
        const rule = ROUTE_MAP[page];

        if (!rule) return;
        if (rule === 'guest')    { requireGuest(); return; }
        if (rule === 'auth')     { requireAuth();  return; }
        if (Array.isArray(rule)) { requireRole(...rule); }
    })();

    // Public API
    return {
        isLoggedIn,
        getUser,
        requireAuth,
        requireGuest,
        requireRole,
        redirectAfterLogin,
        authFetch,
        logout
    };

})();

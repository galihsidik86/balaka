package com.artivisi.accountingfinance.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.header.HeaderWriter;

/**
 * Custom CSP header writer that uses per-request nonce values.
 *
 * This replaces 'unsafe-inline' and 'unsafe-eval' with cryptographically
 * secure nonces, significantly improving XSS protection while maintaining
 * compatibility with Alpine.js and HTMX.
 */
public class CspNonceHeaderWriter implements HeaderWriter {

    private static final String CSP_HEADER = "Content-Security-Policy";

    @Override
    public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
        String nonce = (String) request.getAttribute(CspNonceFilter.CSP_NONCE_ATTRIBUTE);

        if (nonce == null) {
            // Fallback if nonce filter didn't run (shouldn't happen)
            nonce = "missing-nonce";
        }

        // CSP with nonce-based inline scripts/styles - no unsafe-inline or unsafe-eval
        // Alpine CSP build + Alpine.data() components eliminate need for Function() constructor
        // See: https://alpinejs.dev/advanced/csp
        String cspPolicy = String.format(
            "default-src 'self'; " +
            "script-src 'self' 'nonce-%s' https://cdn.jsdelivr.net https://unpkg.com; " +
            "style-src 'self' 'nonce-%s' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
            "img-src 'self' data: blob:; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'; " +
            "form-action 'self'; " +
            "base-uri 'self'",
            nonce, nonce
        );

        response.setHeader(CSP_HEADER, cspPolicy);
    }
}

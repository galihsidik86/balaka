package com.artivisi.accountingfinance.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates a unique CSP nonce for each HTTP request.
 *
 * The nonce is stored as a request attribute and used in:
 * 1. Content-Security-Policy header (via SecurityConfig)
 * 2. Inline script/style tags (via Thymeleaf templates)
 *
 * This eliminates the need for 'unsafe-inline' and 'unsafe-eval' in CSP,
 * significantly improving XSS protection.
 */
@Component
public class CspNonceFilter extends OncePerRequestFilter {

    public static final String CSP_NONCE_ATTRIBUTE = "cspNonce";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int NONCE_LENGTH = 16; // 128 bits

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        // Generate cryptographically secure random nonce
        byte[] nonceBytes = new byte[NONCE_LENGTH];
        RANDOM.nextBytes(nonceBytes);
        String nonce = Base64.getEncoder().encodeToString(nonceBytes);

        // Store nonce in request attribute for use by SecurityConfig and templates
        request.setAttribute(CSP_NONCE_ATTRIBUTE, nonce);

        filterChain.doFilter(request, response);
    }
}

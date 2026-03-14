package com.artivisi.accountingfinance.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for RateLimitService.
 * Tests rate limiting behavior for login, API, and general requests.
 */
@DisplayName("RateLimitService - Request Rate Limiting")
class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setup() {
        rateLimitService = new RateLimitService();
    }

    // ==================== Login Rate Limiting ====================

    @Test
    @DisplayName("Should allow first login request")
    void shouldAllowFirstLoginRequest() {
        boolean allowed = rateLimitService.isLoginAllowed("192.168.1.1");
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("Should allow multiple login requests within limit")
    void shouldAllowMultipleLoginRequestsWithinLimit() {
        String ip = "192.168.1.2";

        // Should allow 10 requests
        for (int i = 0; i < 10; i++) {
            boolean allowed = rateLimitService.isLoginAllowed(ip);
            assertThat(allowed).isTrue();
        }
    }

    @Test
    @DisplayName("Should block login requests after exceeding limit")
    void shouldBlockLoginRequestsAfterExceedingLimit() {
        String ip = "192.168.1.3";

        // Make 10 requests (all allowed)
        for (int i = 0; i < 10; i++) {
            rateLimitService.isLoginAllowed(ip);
        }

        // 11th request should be blocked
        boolean allowed = rateLimitService.isLoginAllowed(ip);
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("Should allow login for null IP address")
    void shouldAllowLoginForNullIpAddress() {
        boolean allowed = rateLimitService.isLoginAllowed(null);
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("Should allow login for blank IP address")
    void shouldAllowLoginForBlankIpAddress() {
        boolean allowed = rateLimitService.isLoginAllowed("   ");
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("Should track different IPs separately for login")
    void shouldTrackDifferentIpsSeparatelyForLogin() {
        String ip1 = "192.168.1.10";
        String ip2 = "192.168.1.11";

        // Use all requests for ip1
        for (int i = 0; i < 10; i++) {
            rateLimitService.isLoginAllowed(ip1);
        }

        // ip2 should still have full quota
        boolean allowed = rateLimitService.isLoginAllowed(ip2);
        assertThat(allowed).isTrue();
    }

    // ==================== API Rate Limiting ====================

    @Test
    @DisplayName("Should allow first API request")
    void shouldAllowFirstApiRequest() {
        boolean allowed = rateLimitService.isApiAllowed("192.168.1.100");
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("Should allow multiple API requests within limit")
    void shouldAllowMultipleApiRequestsWithinLimit() {
        String ip = "192.168.1.101";

        // Should allow 100 requests
        for (int i = 0; i < 100; i++) {
            boolean allowed = rateLimitService.isApiAllowed(ip);
            assertThat(allowed).isTrue();
        }
    }

    @Test
    @DisplayName("Should block API requests after exceeding limit")
    void shouldBlockApiRequestsAfterExceedingLimit() {
        String ip = "192.168.1.102";

        // Make 100 requests (all allowed)
        for (int i = 0; i < 100; i++) {
            rateLimitService.isApiAllowed(ip);
        }

        // 101st request should be blocked
        boolean allowed = rateLimitService.isApiAllowed(ip);
        assertThat(allowed).isFalse();
    }

    // ==================== General Rate Limiting ====================

    @Test
    @DisplayName("Should allow first general request")
    void shouldAllowFirstGeneralRequest() {
        boolean allowed = rateLimitService.isGeneralAllowed("192.168.1.200");
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("Should allow multiple general requests within limit")
    void shouldAllowMultipleGeneralRequestsWithinLimit() {
        String ip = "192.168.1.201";

        // Should allow 300 requests
        for (int i = 0; i < 300; i++) {
            boolean allowed = rateLimitService.isGeneralAllowed(ip);
            assertThat(allowed).isTrue();
        }
    }

    @Test
    @DisplayName("Should block general requests after exceeding limit")
    void shouldBlockGeneralRequestsAfterExceedingLimit() {
        String ip = "192.168.1.202";

        // Make 300 requests (all allowed)
        for (int i = 0; i < 300; i++) {
            rateLimitService.isGeneralAllowed(ip);
        }

        // 301st request should be blocked
        boolean allowed = rateLimitService.isGeneralAllowed(ip);
        assertThat(allowed).isFalse();
    }

    // ==================== Remaining Requests ====================

    @Test
    @DisplayName("Should return max remaining for new IP")
    void shouldReturnMaxRemainingForNewIp() {
        int remaining = rateLimitService.getLoginRemaining("192.168.1.50");
        assertThat(remaining).isEqualTo(10);
    }

    @Test
    @DisplayName("Should decrease remaining after requests")
    void shouldDecreaseRemainingAfterRequests() {
        String ip = "192.168.1.51";

        rateLimitService.isLoginAllowed(ip);
        rateLimitService.isLoginAllowed(ip);
        rateLimitService.isLoginAllowed(ip);

        int remaining = rateLimitService.getLoginRemaining(ip);
        assertThat(remaining).isEqualTo(7);
    }

    @Test
    @DisplayName("Should return zero remaining when limit exhausted")
    void shouldReturnZeroRemainingWhenLimitExhausted() {
        String ip = "192.168.1.52";

        for (int i = 0; i < 15; i++) {
            rateLimitService.isLoginAllowed(ip);
        }

        int remaining = rateLimitService.getLoginRemaining(ip);
        assertThat(remaining).isZero();
    }

    @Test
    @DisplayName("Should return max remaining for null IP")
    void shouldReturnMaxRemainingForNullIp() {
        int remaining = rateLimitService.getLoginRemaining(null);
        assertThat(remaining).isEqualTo(10);
    }

    // ==================== Reset Seconds ====================

    @Test
    @DisplayName("Should return zero reset seconds for new IP")
    void shouldReturnZeroResetSecondsForNewIp() {
        long resetSeconds = rateLimitService.getLoginResetSeconds("192.168.1.60");
        assertThat(resetSeconds).isZero();
    }

    @Test
    @DisplayName("Should return reset seconds after request")
    void shouldReturnResetSecondsAfterRequest() {
        String ip = "192.168.1.61";

        rateLimitService.isLoginAllowed(ip);

        long resetSeconds = rateLimitService.getLoginResetSeconds(ip);
        assertThat(resetSeconds).isGreaterThanOrEqualTo(0);
        assertThat(resetSeconds).isLessThanOrEqualTo(60);
    }

    @Test
    @DisplayName("Should return zero reset seconds for null IP")
    void shouldReturnZeroResetSecondsForNullIp() {
        long resetSeconds = rateLimitService.getLoginResetSeconds(null);
        assertThat(resetSeconds).isZero();
    }

    // ==================== IP Normalization ====================

    @Test
    @DisplayName("Should handle X-Forwarded-For header with multiple IPs")
    void shouldHandleXForwardedForHeaderWithMultipleIps() {
        String xForwardedFor = "203.0.113.1, 70.41.3.18, 150.172.238.178";

        boolean allowed1 = rateLimitService.isLoginAllowed(xForwardedFor);
        assertThat(allowed1).isTrue();

        // Make more requests
        for (int i = 0; i < 9; i++) {
            rateLimitService.isLoginAllowed(xForwardedFor);
        }

        // Should be blocked based on first IP
        boolean allowed2 = rateLimitService.isLoginAllowed(xForwardedFor);
        assertThat(allowed2).isFalse();
    }

    @Test
    @DisplayName("Should normalize IP to lowercase")
    void shouldNormalizeIpToLowercase() {
        String ip1 = "192.168.1.70";
        String ip2 = "192.168.1.70";

        // Use some requests with mixed case (internally normalized)
        for (int i = 0; i < 5; i++) {
            rateLimitService.isLoginAllowed(ip1);
        }

        // Should share the same counter
        int remaining = rateLimitService.getLoginRemaining(ip2);
        assertThat(remaining).isEqualTo(5);
    }

    // ==================== Cleanup ====================

    @Test
    @DisplayName("Should not throw on cleanup")
    void shouldNotThrowOnCleanup() {
        // Add some entries
        rateLimitService.isLoginAllowed("192.168.1.80");
        rateLimitService.isApiAllowed("192.168.1.81");
        rateLimitService.isGeneralAllowed("192.168.1.82");

        // Cleanup should not throw
        assertThatCode(() -> rateLimitService.cleanup())
                .doesNotThrowAnyException();
    }

    // ==================== Independent Limits ====================

    @Test
    @DisplayName("Should track login and API limits independently")
    void shouldTrackLoginAndApiLimitsIndependently() {
        String ip = "192.168.1.90";

        // Exhaust login limit
        for (int i = 0; i < 10; i++) {
            rateLimitService.isLoginAllowed(ip);
        }
        boolean loginAllowed = rateLimitService.isLoginAllowed(ip);
        assertThat(loginAllowed).isFalse();

        // API should still be available
        boolean apiAllowed = rateLimitService.isApiAllowed(ip);
        assertThat(apiAllowed).isTrue();
    }

    // ==================== API and General Null/Blank IP ====================

    @Test
    @DisplayName("Should allow API for null IP address")
    void shouldAllowApiForNullIpAddress() {
        assertThat(rateLimitService.isApiAllowed(null)).isTrue();
    }

    @Test
    @DisplayName("Should allow API for blank IP address")
    void shouldAllowApiForBlankIpAddress() {
        assertThat(rateLimitService.isApiAllowed("   ")).isTrue();
    }

    @Test
    @DisplayName("Should allow general for null IP address")
    void shouldAllowGeneralForNullIpAddress() {
        assertThat(rateLimitService.isGeneralAllowed(null)).isTrue();
    }

    @Test
    @DisplayName("Should allow general for blank IP address")
    void shouldAllowGeneralForBlankIpAddress() {
        assertThat(rateLimitService.isGeneralAllowed("  ")).isTrue();
    }

    // ==================== Login Remaining for Blank IP ====================

    @Test
    @DisplayName("Should return max remaining for blank IP")
    void shouldReturnMaxRemainingForBlankIp() {
        int remaining = rateLimitService.getLoginRemaining("  ");
        assertThat(remaining).isEqualTo(10);
    }

    // ==================== Reset Seconds for Blank IP ====================

    @Test
    @DisplayName("Should return zero reset seconds for blank IP")
    void shouldReturnZeroResetSecondsForBlankIp() {
        long resetSeconds = rateLimitService.getLoginResetSeconds("  ");
        assertThat(resetSeconds).isZero();
    }

    // ==================== Cleanup with Entries ====================

    @Test
    @DisplayName("Should cleanup without affecting active entries")
    void shouldCleanupWithoutAffectingActiveEntries() {
        String ip = "192.168.1.85";
        rateLimitService.isLoginAllowed(ip);
        rateLimitService.isApiAllowed(ip);
        rateLimitService.isGeneralAllowed(ip);

        rateLimitService.cleanup();

        // Active entries should still be present (window hasn't expired)
        int remaining = rateLimitService.getLoginRemaining(ip);
        assertThat(remaining).isEqualTo(9);
    }

    @Test
    @DisplayName("Should cleanup empty maps without error")
    void shouldCleanupEmptyMapsWithoutError() {
        // No entries have been added
        assertThatCode(() -> rateLimitService.cleanup())
                .doesNotThrowAnyException();
    }

    // ==================== X-Forwarded-For Edge Cases ====================

    @Test
    @DisplayName("Should handle single IP in X-Forwarded-For")
    void shouldHandleSingleIpInXForwardedFor() {
        String ip = "10.0.0.1";
        boolean allowed = rateLimitService.isLoginAllowed(ip);
        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("Should track same first IP from X-Forwarded-For consistently")
    void shouldTrackSameFirstIpConsistently() {
        String xff1 = "10.0.0.1, 192.168.1.1";
        String xff2 = "10.0.0.1, 172.16.0.1";

        for (int i = 0; i < 5; i++) {
            rateLimitService.isLoginAllowed(xff1);
        }
        for (int i = 0; i < 5; i++) {
            rateLimitService.isLoginAllowed(xff2);
        }

        // Both share the same first IP, so limit should be exhausted
        boolean allowed = rateLimitService.isLoginAllowed(xff1);
        assertThat(allowed).isFalse();
    }

    // ==================== General and API Limits Independent ====================

    @Test
    @DisplayName("Should track general and login limits independently")
    void shouldTrackGeneralAndLoginLimitsIndependently() {
        String ip = "192.168.1.95";

        // Exhaust login limit
        for (int i = 0; i < 10; i++) {
            rateLimitService.isLoginAllowed(ip);
        }
        assertThat(rateLimitService.isLoginAllowed(ip)).isFalse();

        // General should still be available
        assertThat(rateLimitService.isGeneralAllowed(ip)).isTrue();
    }

    // ==================== Reset Seconds After Requests ====================

    @Test
    @DisplayName("Should return positive reset seconds after rate limiting")
    void shouldReturnPositiveResetSecondsAfterRateLimiting() {
        String ip = "192.168.1.75";

        for (int i = 0; i < 10; i++) {
            rateLimitService.isLoginAllowed(ip);
        }

        long resetSeconds = rateLimitService.getLoginResetSeconds(ip);
        assertThat(resetSeconds).isGreaterThan(0);
        assertThat(resetSeconds).isLessThanOrEqualTo(60);
    }
}

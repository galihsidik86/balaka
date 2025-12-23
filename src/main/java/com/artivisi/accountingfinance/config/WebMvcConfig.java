package com.artivisi.accountingfinance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the application.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CspNonceInterceptor cspNonceInterceptor;

    public WebMvcConfig(CspNonceInterceptor cspNonceInterceptor) {
        this.cspNonceInterceptor = cspNonceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add CSP nonce to all Thymeleaf templates
        registry.addInterceptor(cspNonceInterceptor);
    }
}

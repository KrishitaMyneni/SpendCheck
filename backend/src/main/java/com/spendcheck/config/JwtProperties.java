package com.spendcheck.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key used for signing and verifying JWT tokens.
     * Configured via JWT_SECRET environment variable.
     */
    private String secret;

    /**
     * JWT token expiration duration in milliseconds (default 24h).
     * Configured via JWT_EXPIRATION_MS environment variable.
     */
    private long expirationMs = 86400000;

    public JwtProperties() {
    }

    public JwtProperties(String secret, long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}

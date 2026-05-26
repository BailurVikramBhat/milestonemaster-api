package com.vikrambhat.milestonemaster.auth.filters.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.auth.login-rate-limit")
public record LoginRateLimiterProperties(boolean enabled, int capacity, int refillTokens, Duration window, String redisKeyPrefix, String trustedProxies) {
}

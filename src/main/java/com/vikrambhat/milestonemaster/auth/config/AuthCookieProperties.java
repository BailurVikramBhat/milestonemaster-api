package com.vikrambhat.milestonemaster.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.cookie")
public record AuthCookieProperties(String name, String path, boolean httpOnly, boolean secure, String sameSite) {
}

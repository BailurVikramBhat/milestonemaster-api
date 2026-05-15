package com.vikrambhat.milestonemaster.auth;

import com.vikrambhat.milestonemaster.auth.config.AuthCookieProperties;
import com.vikrambhat.milestonemaster.auth.config.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCookieService {
    private final AuthCookieProperties authCookieProperties;
    private final JwtProperties jwtProperties;
    public AuthCookieService(AuthCookieProperties authCookieProperties, JwtProperties jwtProperties) {
        this.authCookieProperties = authCookieProperties;
        this.jwtProperties = jwtProperties;
    }
    public void addAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(authCookieProperties.name(), token)
                .httpOnly(authCookieProperties.httpOnly())
                .secure(authCookieProperties.secure())
                .path(authCookieProperties.path())
                .sameSite(authCookieProperties.sameSite())
                .maxAge(Duration.ofMinutes(jwtProperties.expirationMinutes()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

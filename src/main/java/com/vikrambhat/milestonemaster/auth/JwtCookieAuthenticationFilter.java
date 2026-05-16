package com.vikrambhat.milestonemaster.auth;

import com.vikrambhat.milestonemaster.auth.config.AuthCookieProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtCookieAuthenticationFilter.class);

    private final AuthCookieProperties authCookieProperties;
    private final JwtDecoder jwtDecoder;
    private final UserDetailsService userDetailsService;

    public JwtCookieAuthenticationFilter(AuthCookieProperties authCookieProperties, JwtDecoder jwtDecoder, UserDetailsService userDetailsService) {
        this.authCookieProperties = authCookieProperties;
        this.jwtDecoder = jwtDecoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractCookieValue(request, authCookieProperties.name());
        if(token !=null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Jwt jwt = jwtDecoder.decode(token);
                UserDetails details = userDetailsService.loadUserByUsername(jwt.getSubject());
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                log.debug("Rejected invalid auth cookie for {} {}", request.getMethod(), request.getRequestURI());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractCookieValue(HttpServletRequest request, String name) {
        if(request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies()).filter((cookie -> name.equals(cookie.getName())))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return "/api/v1/auth/login".equals(path)
                || "/api/v1/auth/logout".equals(path);
    }

}

package com.vikrambhat.milestonemaster.auth.filters;

import com.vikrambhat.milestonemaster.auth.filters.properties.LoginRateLimiterProperties;
import com.vikrambhat.milestonemaster.auth.ratelimit.LoginRateLimiterService;
import com.vikrambhat.milestonemaster.common.utils.IpAddressMask;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoginRateLimitFilter.class);
    private final LoginRateLimiterService loginRateLimiterService;
    private final LoginRateLimiterProperties loginRateLimiterProperties;

    public LoginRateLimitFilter(LoginRateLimiterService loginRateLimiterService, LoginRateLimiterProperties loginRateLimiterProperties) {
        this.loginRateLimiterService = loginRateLimiterService;
        this.loginRateLimiterProperties = loginRateLimiterProperties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if(!loginRateLimiterProperties.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        String clientIp = request.getRemoteAddr();
        Bucket bucket = loginRateLimiterService.resolveBucket(clientIp);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if(!probe.isConsumed()) {
            logger.info("Rate limit reached for ip: {}", IpAddressMask.maskPartialIp(clientIp));
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(waitSeconds));
            response.setHeader("X-RateLimit-Limit", String.valueOf(loginRateLimiterProperties.capacity()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.getWriter().write("Too many requests, please try again later.");
            return;
        }
        response.setHeader("X-RateLimit-Limit", String.valueOf(loginRateLimiterProperties.capacity()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        filterChain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().equals("/api/v1/auth/login")
                || !request.getMethod().equalsIgnoreCase("POST");
    }
}

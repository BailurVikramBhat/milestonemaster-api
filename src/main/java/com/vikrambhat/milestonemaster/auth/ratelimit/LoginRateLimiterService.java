package com.vikrambhat.milestonemaster.auth.ratelimit;

import com.vikrambhat.milestonemaster.auth.filters.properties.LoginRateLimiterProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiterService {
    private final LoginRateLimiterProperties loginRateLimiterProperties;

    public LoginRateLimiterService(LoginRateLimiterProperties loginRateLimiterProperties) {
        this.loginRateLimiterProperties = loginRateLimiterProperties;
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    public Bucket resolveBucket(String key) {

        return buckets.computeIfAbsent(key, this::createNewBucket);
    }
    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(loginRateLimiterProperties.capacity())
                .refillGreedy(loginRateLimiterProperties.refillTokens(), loginRateLimiterProperties.window())
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}

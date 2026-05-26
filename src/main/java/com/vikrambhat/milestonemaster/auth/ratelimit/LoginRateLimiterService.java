package com.vikrambhat.milestonemaster.auth.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiterService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    public Bucket resolveBucket(String key) {

        return buckets.computeIfAbsent(key, this::createNewBucket);
    }
    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(10))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}

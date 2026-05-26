package com.vikrambhat.milestonemaster.auth.ratelimit;

import com.vikrambhat.milestonemaster.auth.filters.properties.LoginRateLimiterProperties;
import com.vikrambhat.milestonemaster.user.Role;
import com.vikrambhat.milestonemaster.user.User;
import com.vikrambhat.milestonemaster.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.auth.login-rate-limit.enabled=true",
        "app.auth.login-rate-limit.capacity=5",
        "app.auth.login-rate-limit.refill-tokens=5",
        "app.auth.login-rate-limit.window=10s"
})
class LoginRateLimitFilterTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    LoginRateLimiterProperties loginRateLimiterProperties;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(new User("test@milestonemaster.com", passwordEncoder.encode("Simple123@"), "Test User", Role.USER));
    }

    @Test
    void login_withValidCredentials_sixthRequestReturnsTooManyRequests() throws Exception {
        Map<String, String> request = Map.of(
                "email", "test@milestonemaster.com",
                "password", "Simple123@"
        );

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(requestPostProcessor -> {
                                requestPostProcessor.setRemoteAddr("192.0.2.10");
                                return requestPostProcessor;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-RateLimit-Limit", "5"))
                    .andExpect(header().exists("X-RateLimit-Remaining"));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(requestPostProcessor -> {
                            requestPostProcessor.setRemoteAddr("192.0.2.10");
                            return requestPostProcessor;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(header().string("X-RateLimit-Limit", "5"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(content().string("Too many requests, please try again later."));
    }
    @Test
    void login_withInValidCredentials_sixthRequestReturnsTooManyRequests() throws Exception {
        Map<String, String> request = Map.of(
                "email", "test@milestonemaster.com",
                "password", "Simple123#"
        );

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(requestPostProcessor -> {
                                requestPostProcessor.setRemoteAddr("192.0.2.11");
                                return requestPostProcessor;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("X-RateLimit-Limit", "5"))
                    .andExpect(header().exists("X-RateLimit-Remaining"));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(requestPostProcessor -> {
                            requestPostProcessor.setRemoteAddr("192.0.2.11");
                            return requestPostProcessor;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(header().string("X-RateLimit-Limit", "5"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(content().string("Too many requests, please try again later."));
    }
    @Test
    void login_withValidCredentials_afterRateLimitDurationReturnsOk() throws Exception {
        Map<String, String> request = Map.of(
                "email", "test@milestonemaster.com",
                "password", "Simple123@"
        );

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(requestPostProcessor -> {
                                requestPostProcessor.setRemoteAddr("192.0.2.12");
                                return requestPostProcessor;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-RateLimit-Limit", "5"))
                    .andExpect(header().exists("X-RateLimit-Remaining"));
        }
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(requestPostProcessor -> {
                            requestPostProcessor.setRemoteAddr("192.0.2.12");
                            return requestPostProcessor;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(header().string("X-RateLimit-Limit", "5"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(content().string("Too many requests, please try again later."));

        Duration singleTokenRefillDuration = loginRateLimiterProperties.window()
                .dividedBy(loginRateLimiterProperties.refillTokens())
                .plusMillis(250);
        Thread.sleep(singleTokenRefillDuration);
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(requestPostProcessor -> {
                            requestPostProcessor.setRemoteAddr("192.0.2.12");
                            return requestPostProcessor;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.RETRY_AFTER))
                .andExpect(header().string("X-RateLimit-Limit", "5"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(jsonPath("$.userId").exists());

    }
}

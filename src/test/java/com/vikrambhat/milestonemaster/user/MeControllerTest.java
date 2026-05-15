package com.vikrambhat.milestonemaster.user;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class MeControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtEncoder jwtEncoder;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(new User(
                "test@milestonemaster.com",
                passwordEncoder.encode("Simple123@"),
                "Test User",
                Role.USER
        ));
    }

    private Cookie loginAndSetAuthCookie() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "email", "test@milestonemaster.com",
                "password", "Simple123@"
        );

        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("MM_AUTH"))
                .andReturn()
                .getResponse().getCookie("MM_AUTH");
    }

    private Cookie expiredAuthCookieFor(String email) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .issuedAt(now.minus(30, ChronoUnit.MINUTES))
                .expiresAt(now.minus(15, ChronoUnit.MINUTES))
                .build();
        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(headers, claims))
                .getTokenValue();
        return new Cookie("MM_AUTH", token);
    }

    @Test
    void me_withoutAuthCookie_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withValidAuthCookie_returnsCurrentUser() throws Exception {
        Cookie authCookie = loginAndSetAuthCookie();
        mockMvc.perform(get("/api/v1/me").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@milestonemaster.com"))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(content().string(not(containsString("password"))))
                .andExpect(content().string(not(containsString("passwordHash"))))
                .andExpect(content().string(not(containsString("\"id\""))));
    }

    @Test
    void me_withForgedAuthCookie_returnsUnauthorized() throws Exception {
        Cookie invalidCookie = new Cookie("MM_AUTH", loginAndSetAuthCookie().getValue()+"forged");
        mockMvc.perform(get("/api/v1/me").cookie(invalidCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withInvalidAuthCookie_returnsUnauthorized() throws Exception {
        Cookie invalidCookie = new Cookie("MM_AUTH", "invalid-auth-cookie");
        mockMvc.perform(get("/api/v1/me").cookie(invalidCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withExpiredAuthCookie_returnsUnauthorized() throws Exception {
        Cookie expiredCookie = expiredAuthCookieFor("test@milestonemaster.com");
        mockMvc.perform(get("/api/v1/me").cookie(expiredCookie))
                .andExpect(status().isUnauthorized());
    }

}
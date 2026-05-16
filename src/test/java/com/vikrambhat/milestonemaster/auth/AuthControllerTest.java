package com.vikrambhat.milestonemaster.auth;

import com.vikrambhat.milestonemaster.common.logging.RequestIdFilter;
import com.vikrambhat.milestonemaster.user.Role;
import com.vikrambhat.milestonemaster.user.User;
import com.vikrambhat.milestonemaster.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
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
        userRepository.save(new User("test@milestonemaster.com", passwordEncoder.encode("Simple123@"), "Test User", Role.USER));
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

    @Nested
    @DisplayName("POST /login")
    class Login {
        @Test
        void login_withValidCredentials_returnsOkAndSetsHttpOnlyCookie() throws Exception {
            Map<String, String> request = Map.of("email", "test@milestonemaster.com", "password", "Simple123@");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(cookie().exists("MM_AUTH"))
                    .andExpect(cookie().httpOnly("MM_AUTH", true))
                    .andExpect(cookie().path("MM_AUTH", "/"))
                    .andExpect(cookie().sameSite("MM_AUTH", "Lax"))
                    .andExpect(cookie().maxAge("MM_AUTH", 900))
                    .andExpect(jsonPath("$.email").value("test@milestonemaster.com"))
                    .andExpect(jsonPath("$.fullName").value("Test User"))
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(content().string(not(containsString("token"))))
                    .andExpect(content().string(not(containsString("jwt"))))
                    .andExpect(content().string(not(containsString("MM_AUTH"))))
                    .andExpect(content().string(not(containsString("accessToken"))));
        }

        @Test
        void login_withRequestId_returnsSameRequestIdHeader() throws Exception {
            String requestId = "test-request-id";
            Map<String, String> request = Map.of("email", "test@milestonemaster.com", "password", "Simple123@");
            mockMvc.perform(post("/api/v1/auth/login")
                            .header(RequestIdFilter.REQUEST_ID_HEADER, requestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(RequestIdFilter.REQUEST_ID_HEADER, requestId));
        }

        @Test
        void login_withIncorrectEmail_returnsUnauthorizedAndDoesNotSetHttpOnlyCookie() throws Exception {
            Map<String, String> request = Map.of("email", "dev@milestonemaster.com", "password", "Simple123@");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                    .andExpect(cookie().doesNotExist("MM_AUTH"))
                    .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                    .andExpect(jsonPath("$.message").value("Invalid email or password"))
                    .andExpect(jsonPath("$.uri").exists())
                    .andExpect(content().string(not(containsString("userId"))))
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(content().string(not(containsString("fullName"))));
        }
        @Test
        void login_withCorrectEmailIncorrectPassword_returnsUnauthorizedAndDoesNotSetHttpOnlyCookie() throws Exception {
            Map<String, String> request = Map.of("email", "test@milestonemaster.com", "password", "Simple123#");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                    .andExpect(cookie().doesNotExist("MM_AUTH"))
                    .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                    .andExpect(jsonPath("$.message").value("Invalid email or password"))
                    .andExpect(jsonPath("$.uri").exists())
                    .andExpect(content().string(not(containsString("userId"))))
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(content().string(not(containsString("fullName"))));
        }
    }

    @Nested
    @DisplayName("Login Validations")
    class LoginValidation {
        @Test
        void login_withIncorrectEmailFormat_returnsBadRequestAndDoesNotSetHttpOnlyCookie() throws Exception {
            Map<String, String> request = Map.of("email", "teslestonemaster.com", "password", "Simple123#");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .andExpect(jsonPath("$.uri").exists())
                    .andExpect(jsonPath("$.errors.email").value("Please enter a valid email"));
        }

        @Test
        void login_withBlankEmail_returnsBadRequestAndDoesNotSetHttpOnlyCookie() throws Exception {
            Map<String, String> request = Map.of("email", "", "password", "Simple123@");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .andExpect(jsonPath("$.uri").exists())
                    .andExpect(jsonPath("$.errors.email").value("Email cannot be blank"));
        }

        @Test
        void login_withBlankPassword_returnsBadRequestAndDoesNotSetHttpOnlyCookie() throws Exception {
            Map<String, String> request = Map.of("email", "test@milestonemaster.com", "password", "");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .andExpect(jsonPath("$.uri").exists())
                    .andExpect(jsonPath("$.errors.password").value("Password cannot be blank"));
        }

        @Test
        void login_withMultipleValidationErrors_returnsBadRequestAndResponseContainsMultipleValidationFailMessages() throws Exception {
            Map<String, String> request = Map.of("email", "teslestonemaster.com", "password", "");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.message").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .andExpect(jsonPath("$.uri").exists())
                    .andExpect(jsonPath("$.errors.email").value("Please enter a valid email"))
                    .andExpect(jsonPath("$.errors.password").value("Password cannot be blank"));
        }
    }

    @Nested
    @DisplayName("POST /logout")
    class Logout {
        @Test
        void withValidAuthCookie_returnsOkAndClearsCookie() throws Exception {
            Cookie cookie = loginAndSetAuthCookie();
            mockMvc.perform(post("/api/v1/auth/logout").cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(cookie().exists("MM_AUTH"))
                    .andExpect(cookie().httpOnly("MM_AUTH", true))
                    .andExpect(cookie().path("MM_AUTH", "/"))
                    .andExpect(cookie().sameSite("MM_AUTH", "Lax"))
                    .andExpect(cookie().maxAge("MM_AUTH", 0))
                    .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }

        @Test
        void withNoAuthCookie_returnsOkAndClearsCookie() throws Exception {
            mockMvc.perform(post("/api/v1/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(cookie().exists("MM_AUTH"))
                    .andExpect(cookie().httpOnly("MM_AUTH", true))
                    .andExpect(cookie().path("MM_AUTH", "/"))
                    .andExpect(cookie().sameSite("MM_AUTH", "Lax"))
                    .andExpect(cookie().maxAge("MM_AUTH", 0))
                    .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }

        @Test
        void withExpiredAuthCookie_returnsOkAndClearsCookie() throws Exception {
            Cookie cookie = expiredAuthCookieFor("test@milestonemaster.com");
            mockMvc.perform(post("/api/v1/auth/logout").cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(cookie().exists("MM_AUTH"))
                    .andExpect(cookie().httpOnly("MM_AUTH", true))
                    .andExpect(cookie().path("MM_AUTH", "/"))
                    .andExpect(cookie().sameSite("MM_AUTH", "Lax"))
                    .andExpect(cookie().maxAge("MM_AUTH", 0))
                    .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }

        @Test
        void clearCookieUsesSameNamePathHttpOnlySameSiteSecurePolicy() throws Exception {
            Cookie cookie = loginAndSetAuthCookie();
            mockMvc.perform(post("/api/v1/auth/logout").cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("MM_AUTH"))
                    .andExpect(cookie().path("MM_AUTH", "/"))
                    .andExpect(cookie().httpOnly("MM_AUTH", true))
                    .andExpect(cookie().sameSite("MM_AUTH", "Lax"))
                    .andExpect(cookie().secure("MM_AUTH", false))
                    .andExpect(cookie().maxAge("MM_AUTH", 0));
        }
    }

}

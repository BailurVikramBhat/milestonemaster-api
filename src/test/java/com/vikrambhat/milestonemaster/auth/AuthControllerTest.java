package com.vikrambhat.milestonemaster.auth;

import com.vikrambhat.milestonemaster.user.Role;
import com.vikrambhat.milestonemaster.user.User;
import com.vikrambhat.milestonemaster.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(new User("test@milestonemaster.com", passwordEncoder.encode("Simple123@"), "Test User", Role.USER));
    }

    @Test
    void login_withValidCredentials_returnsOkAndSetsHttpOnlyCookie() throws Exception {
        Map<String, String> request = Map.of("email", "test@milestonemaster.com", "password", "Simple123@");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
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
package com.vikrambhat.milestonemaster.flows;

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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class AuthFlowTest {
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
    @Nested
    @DisplayName("FLOW TESTS: Login - Logout - GET /me")
    class LoginLogout {
        @Test
        void me_afterLogoutWithoutFreshLogin_returnsUnauthorized() throws Exception {
            Cookie cookie = loginAndSetAuthCookie();
            mockMvc.perform(post("/api/v1/auth/logout").cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(cookie().maxAge("MM_AUTH", 0));
            mockMvc.perform(get("/api/v1/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}

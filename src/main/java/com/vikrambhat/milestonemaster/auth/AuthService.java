package com.vikrambhat.milestonemaster.auth;

import com.vikrambhat.milestonemaster.auth.dto.LoginRequest;
import com.vikrambhat.milestonemaster.auth.dto.LoginResponse;
import com.vikrambhat.milestonemaster.common.logging.LogSanitizer;
import com.vikrambhat.milestonemaster.common.utils.EmailFormatter;
import com.vikrambhat.milestonemaster.user.User;
import com.vikrambhat.milestonemaster.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthCookieService authCookieService;

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService, AuthCookieService authCookieService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authCookieService = authCookieService;
    }

    public LoginResult login(LoginRequest request, HttpServletResponse servletResponse) {
        String email = EmailFormatter.normalize(request.email());
        // TODO: use result of this for @AuthenticationPrincipal with customDTO
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException ex) {
            log.warn("Login failed for email={}", LogSanitizer.maskEmail(email));
            throw ex;
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow();
        String token = jwtService.createAccessToken(user.getEmail());
        LoginResponse response = new LoginResponse(user.getPublicId(), user.getEmail(), user.getFullName());
        LoginResult result = new LoginResult(response, token);
        authCookieService.addAuthCookie(servletResponse, result.token());
        log.info("Login succeeded for userId={}", user.getPublicId());
        return result;
    }

    public void logout(HttpServletResponse response) {
        authCookieService.clearAuthCookie(response);
        log.info("Logout completed");
    }

    public record LoginResult(LoginResponse response, String token) {
    }
}

package com.vikrambhat.milestonemaster.auth;

import com.vikrambhat.milestonemaster.auth.dto.LoginRequest;
import com.vikrambhat.milestonemaster.auth.dto.LoginResponse;
import com.vikrambhat.milestonemaster.common.utils.EmailFormatter;
import com.vikrambhat.milestonemaster.user.User;
import com.vikrambhat.milestonemaster.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
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
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow();
        String token = jwtService.createAccessToken(user.getEmail());
        LoginResponse response = new LoginResponse(user.getPublicId(), user.getEmail(), user.getFullName());
        LoginResult result = new LoginResult(response, token);
        authCookieService.addAuthCookie(servletResponse, result.token());
        return result;
    }

    public void logout(HttpServletResponse response) {
        authCookieService.clearAuthCookie(response);
    }

    public record LoginResult(LoginResponse response, String token) {
    }
}

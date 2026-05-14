package com.vikrambhat.milestonemaster.auth;

import com.vikrambhat.milestonemaster.auth.dto.LoginRequest;
import com.vikrambhat.milestonemaster.auth.dto.LoginResponse;
import com.vikrambhat.milestonemaster.common.utils.EmailFormatter;
import com.vikrambhat.milestonemaster.user.User;
import com.vikrambhat.milestonemaster.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResult login(LoginRequest request) {
        String email = EmailFormatter.normalize(request.email());
        // TODO: use result of this for @AuthenticationPrincipal with customDTO
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow();
        String token = jwtService.createAccessToken(user.getEmail());
        LoginResponse response = new LoginResponse(user.getPublicId(), user.getEmail(), user.getFullName());
        return new LoginResult(response, token);
    }

    public record LoginResult(LoginResponse response, String token) {
    }
}

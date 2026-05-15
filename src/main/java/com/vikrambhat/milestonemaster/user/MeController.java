package com.vikrambhat.milestonemaster.user;

import com.vikrambhat.milestonemaster.user.dto.MeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {
    private final MeService meService;

    public MeController(MeService meService) {
        this.meService = meService;
    }
    @GetMapping
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal UserDetails details) {
        return ResponseEntity.ok(meService.getMe(details.getUsername()));
    }
}

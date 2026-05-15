package com.vikrambhat.milestonemaster.user.dto;


import java.util.UUID;

public record MeResponse(
        UUID userId,
        String email,
        String fullName,
        String role
) {
}

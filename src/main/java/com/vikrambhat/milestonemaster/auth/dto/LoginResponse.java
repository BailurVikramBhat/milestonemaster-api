package com.vikrambhat.milestonemaster.auth.dto;

import java.util.UUID;

public record LoginResponse(UUID userId, String email, String fullName) {
}

package com.vikrambhat.milestonemaster.user;

import com.vikrambhat.milestonemaster.common.logging.LogSanitizer;
import com.vikrambhat.milestonemaster.common.utils.EmailFormatter;
import com.vikrambhat.milestonemaster.user.dto.MeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class MeService {
    private static final Logger log = LoggerFactory.getLogger(MeService.class);

    private final UserRepository userRepository;

    public MeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public MeResponse getMe(String email) {
        String normalizedEmail = EmailFormatter.normalize(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Authenticated principal has no matching user email={}", LogSanitizer.maskEmail(normalizedEmail));
                    return new NoSuchElementException("Current user not found");
                });
        return new MeResponse(user.getPublicId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }
}

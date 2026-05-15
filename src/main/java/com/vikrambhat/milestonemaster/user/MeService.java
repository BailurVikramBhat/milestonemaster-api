package com.vikrambhat.milestonemaster.user;

import com.vikrambhat.milestonemaster.common.utils.EmailFormatter;
import com.vikrambhat.milestonemaster.user.dto.MeResponse;
import org.springframework.stereotype.Service;

@Service
public class MeService {
    private final UserRepository userRepository;

    public MeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public MeResponse getMe(String email) {
        User user = userRepository.findByEmail(EmailFormatter.normalize(email)).orElseThrow();
        return new MeResponse(user.getPublicId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }
}

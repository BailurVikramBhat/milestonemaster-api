package com.vikrambhat.milestonemaster.dev;

import com.vikrambhat.milestonemaster.common.utils.EmailFormatter;
import com.vikrambhat.milestonemaster.user.Role;
import com.vikrambhat.milestonemaster.user.User;
import com.vikrambhat.milestonemaster.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private static final String DEV_EMAIL = "dev@milestonemaster.com";
    private static final String DEV_PASSWORD = "Simple123@";
    private static final String DEV_FULL_NAME = "Dev User";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public void run(String... args) throws Exception {
        String email = EmailFormatter.normalize(DEV_EMAIL);
        if(userRepository.existsByEmail(email)) {
            log.debug("Dev user seed skipped because user already exists");
            return;
        }
        User user = new User(email, passwordEncoder.encode(DEV_PASSWORD), DEV_FULL_NAME, Role.USER);
        User savedUser = userRepository.save(user);
        log.info("Dev user seeded userId={}", savedUser.getPublicId());
    }
}

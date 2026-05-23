package com.vikrambhat.milestonemaster.dev;

import com.vikrambhat.milestonemaster.common.utils.EmailFormatter;
import com.vikrambhat.milestonemaster.user.Role;
import com.vikrambhat.milestonemaster.user.User;
import com.vikrambhat.milestonemaster.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("testenv")
public class TestEnvDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(TestEnvDataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String qaEmail;
    private final String qaPassword;
    private final String qaFullName;

    public TestEnvDataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.qa-seed.email}") String qaEmail,
            @Value("${app.qa-seed.password}") String qaPassword,
            @Value("${app.qa-seed.full-name}") String qaFullName
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.qaEmail = qaEmail;
        this.qaPassword = qaPassword;
        this.qaFullName = qaFullName;
    }

    @Override
    public void run(String... args) {
        String email = EmailFormatter.normalize(qaEmail);
        if (userRepository.existsByEmail(email)) {
            log.debug("QA seed user skipped because user already exists");
            return;
        }

        User user = new User(email, passwordEncoder.encode(qaPassword), qaFullName, Role.USER);
        User savedUser = userRepository.save(user);
        log.info("QA seed user created userId={}", savedUser.getPublicId());
    }
}

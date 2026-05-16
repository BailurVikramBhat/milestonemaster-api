package com.vikrambhat.milestonemaster.auth;

import com.vikrambhat.milestonemaster.common.logging.LogSanitizer;
import com.vikrambhat.milestonemaster.common.utils.EmailFormatter;
import com.vikrambhat.milestonemaster.user.UserRepository;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(DatabaseUserDetailsService.class);

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        String email = EmailFormatter.normalize(username);
        return userRepository.findByEmail(email)
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name())
                        .build())
                .orElseThrow(() -> {
                    log.debug("User details lookup failed for email={}", LogSanitizer.maskEmail(email));
                    return new UsernameNotFoundException("User not found");
                });
    }
}

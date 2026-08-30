package com.buildit.config;

import com.buildit.entity.User;
import com.buildit.enums.UserRole;
import com.buildit.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapUsername;
    private final String bootstrapEmail;
    private final String bootstrapPassword;

    public AdminBootstrapRunner(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 @Value("${admin.bootstrap.username}") String bootstrapUsername,
                                 @Value("${admin.bootstrap.email}") String bootstrapEmail,
                                 @Value("${admin.bootstrap.password}") String bootstrapPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapUsername = bootstrapUsername;
        this.bootstrapEmail = bootstrapEmail;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }

        if (!StringUtils.hasText(bootstrapUsername) || !StringUtils.hasText(bootstrapEmail)
                || !StringUtils.hasText(bootstrapPassword)) {
            log.warn("No admin account exists and ADMIN_BOOTSTRAP_USERNAME/EMAIL/PASSWORD are not fully set — "
                + "set all three to create one automatically, or insert one manually.");
            return;
        }

        if (bootstrapPassword.length() < 8) {
            log.error("ADMIN_BOOTSTRAP_PASSWORD must be at least 8 characters — bootstrap admin not created.");
            return;
        }

        User admin = new User();
        admin.setUsername(bootstrapUsername);
        admin.setEmail(bootstrapEmail);
        admin.setPassword(passwordEncoder.encode(bootstrapPassword));
        admin.setRole(UserRole.ADMIN);

        try {
            userRepository.save(admin);
            log.info("Bootstrap admin '{}' created.", bootstrapUsername);
        } catch (DataIntegrityViolationException ex) {
            log.error("Could not create bootstrap admin '{}' — username or email already in use.", bootstrapUsername);
        }
    }
}

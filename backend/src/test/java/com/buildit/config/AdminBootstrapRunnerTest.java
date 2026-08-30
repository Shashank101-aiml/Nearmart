package com.buildit.config;

import com.buildit.entity.User;
import com.buildit.enums.UserRole;
import com.buildit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private AdminBootstrapRunner runnerWith(String username, String email, String password) {
        return new AdminBootstrapRunner(userRepository, passwordEncoder, username, email, password);
    }

    @Test
    void runDoesNothingWhenAdminAlreadyExists() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);

        runnerWith("bootstrapadmin", "admin@example.com", "password123").run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void runCreatesAdminWhenNoneExistsAndVarsAreSet() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-hash");

        runnerWith("bootstrapadmin", "admin@example.com", "password123").run(null);

        verify(userRepository).save(argThat((User u) ->
            u.getUsername().equals("bootstrapadmin")
                && u.getEmail().equals("admin@example.com")
                && u.getPassword().equals("encoded-hash")
                && u.getRole() == UserRole.ADMIN));
    }

    @Test
    void runSkipsWhenBootstrapVarsAreMissing() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);

        runnerWith("", "admin@example.com", "password123").run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void runSkipsWhenPasswordTooShort() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);

        runnerWith("bootstrapadmin", "admin@example.com", "short").run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void runDoesNotThrowWhenSaveFailsWithDuplicate() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatCode(() -> runnerWith("bootstrapadmin", "admin@example.com", "password123").run(null))
            .doesNotThrowAnyException();
    }
}

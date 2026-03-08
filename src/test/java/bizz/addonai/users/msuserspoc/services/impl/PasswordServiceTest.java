package bizz.addonai.users.msuserspoc.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService(passwordEncoder);
    }

    // --- encryptPassword ---

    @Test
    void encryptPassword_nullPassword_throwsIllegalArgument() {
        assertThatThrownBy(() -> passwordService.encryptPassword(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty");
    }

    @Test
    void encryptPassword_emptyPassword_throwsIllegalArgument() {
        assertThatThrownBy(() -> passwordService.encryptPassword(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty");
    }

    @Test
    void encryptPassword_validPassword_callsEncoder() {
        when(passwordEncoder.encode("secret")).thenReturn("$2a$12$hashed");

        String result = passwordService.encryptPassword("secret");

        assertThat(result).isEqualTo("$2a$12$hashed");
        verify(passwordEncoder).encode("secret");
    }

    // --- verifyPassword ---

    @Test
    void verifyPassword_nullPlainPassword_returnsFalse() {
        boolean result = passwordService.verifyPassword(null, "$2a$12$hash");
        assertThat(result).isFalse();
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void verifyPassword_nullStoredHash_returnsFalse() {
        boolean result = passwordService.verifyPassword("secret", null);
        assertThat(result).isFalse();
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void verifyPassword_validInputs_delegatesToEncoder() {
        when(passwordEncoder.matches("secret", "$2a$12$hash")).thenReturn(true);

        boolean result = passwordService.verifyPassword("secret", "$2a$12$hash");

        assertThat(result).isTrue();
        verify(passwordEncoder).matches("secret", "$2a$12$hash");
    }

    @Test
    void verifyPassword_wrongPassword_returnsFalse() {
        when(passwordEncoder.matches("wrong", "$2a$12$hash")).thenReturn(false);

        boolean result = passwordService.verifyPassword("wrong", "$2a$12$hash");

        assertThat(result).isFalse();
    }

    // --- generateTemporaryPassword ---

    @Test
    void generateTemporaryPassword_returnsCorrectLength() {
        String password = passwordService.generateTemporaryPassword(12);
        assertThat(password).hasSize(12);
    }

    @Test
    void generateTemporaryPassword_eachCallIsDifferent() {
        String p1 = passwordService.generateTemporaryPassword(16);
        String p2 = passwordService.generateTemporaryPassword(16);
        assertThat(p1).isNotEqualTo(p2);
    }

    // --- needsRehash ---

    @Test
    void needsRehash_correctPrefix_returnsFalse() {
        assertThat(passwordService.needsRehash("$2y$12$somehash")).isFalse();
    }

    @Test
    void needsRehash_differentPrefix_returnsTrue() {
        assertThat(passwordService.needsRehash("$2a$12$somehash")).isTrue();
    }

    @Test
    void needsRehash_oldAlgorithm_returnsTrue() {
        assertThat(passwordService.needsRehash("$2b$10$somehash")).isTrue();
    }
}

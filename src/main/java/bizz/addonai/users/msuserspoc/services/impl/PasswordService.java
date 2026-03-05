package bizz.addonai.users.msuserspoc.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }

    public String encryptPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // BCrypt automáticamente genera salt y concatena
        String encrypted = passwordEncoder.encode(plainPassword);

        log.info("Password encrypted successfully (algorithm: BCrypt, length: {})",
                encrypted.length());

        // NUNCA loguear el hash completo en producción
        if (log.isDebugEnabled()) {
            log.debug("Hash prefix: {}", encrypted.substring(0, 7) + "...");
        }

        return encrypted;
    }

    public boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        return passwordEncoder.matches(plainPassword, storedHash);
    }

    public String generateTemporaryPassword(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }

    public boolean needsRehash(String storedHash) {
        return !storedHash.startsWith("$2y$12$");
    }
}
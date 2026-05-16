package com.fitlife.gym.config;

import com.fitlife.gym.crypto.ClientCryptoService;
import com.fitlife.gym.domain.entity.AppUser;
import com.fitlife.gym.domain.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seals legacy plaintext staff fields and migrates demo BCrypt passwords to sealed {@code password}
 * when client PII crypto is enabled.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlaintextStaffPiiMigrationRunner implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "password";

    private final ClientCryptoService clientCrypto;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!clientCrypto.isEnabled()) {
            log.warn(
                    "app.pii.client-storage-secret is not set — staff login expects plaintext app_user.email; "
                            + "encrypted emails in the DB will not match. Set the same secret as the Angular app.");
            return;
        }
        log.info("Staff PII crypto enabled — sealing legacy plaintext app_user rows if needed");
        int updated = 0;
        for (AppUser user : appUserRepository.findAll()) {
            boolean changed = false;
            if (user.getEmail() != null && !ClientCryptoService.isEncrypted(user.getEmail())) {
                user.setEmail(clientCrypto.sealEmail(user.getEmail()));
                changed = true;
            }
            if (user.getFullName() != null && !ClientCryptoService.isEncrypted(user.getFullName())) {
                user.setFullName(clientCrypto.encrypt(user.getFullName().trim()));
                changed = true;
            }
            if (user.getPhone() != null
                    && !user.getPhone().isBlank()
                    && !ClientCryptoService.isEncrypted(user.getPhone())) {
                user.setPhone(clientCrypto.encrypt(user.getPhone().trim()));
                changed = true;
            }
            if (user.getPasswordHash() != null && ClientCryptoService.isBcryptHash(user.getPasswordHash())) {
                user.setPasswordHash(clientCrypto.sealSecret(DEMO_PASSWORD));
                changed = true;
            }
            if (changed) {
                appUserRepository.save(user);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("Sealed staff PII / passwords for {} user(s)", updated);
        }
    }
}

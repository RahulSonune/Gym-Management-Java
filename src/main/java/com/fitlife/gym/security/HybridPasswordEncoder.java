package com.fitlife.gym.security;

import com.fitlife.gym.crypto.ClientCryptoService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Objects;

/**
 * When PII crypto is enabled: store/compare deterministic FLENC1 sealed passwords.
 * Legacy rows keep BCrypt until migrated.
 */
@Component
public class HybridPasswordEncoder implements PasswordEncoder {

    private final ClientCryptoService clientCrypto;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public HybridPasswordEncoder(ClientCryptoService clientCrypto) {
        this.clientCrypto = clientCrypto;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        if (clientCrypto.isEnabled()) {
            return clientCrypto.sealSecret(rawPassword.toString());
        }
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        if (ClientCryptoService.isEncrypted(encodedPassword)) {
            if (!clientCrypto.isEnabled()) {
                return false;
            }
            return constantTimeEquals(
                    clientCrypto.sealSecret(rawPassword.toString()), encodedPassword);
        }
        if (ClientCryptoService.isBcryptHash(encodedPassword)) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }
        return false;
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                Objects.toString(a, "").getBytes(),
                Objects.toString(b, "").getBytes());
    }
}

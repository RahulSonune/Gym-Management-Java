package com.fitlife.gym.crypto;

import com.fitlife.gym.config.PiiProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Base64;

/** Mirrors Angular {@code client-crypto.util.ts} (FLENC1, deterministic IV). */
@Service
public class ClientCryptoService {

    public static final String PREFIX = "FLENC1:";

    private static final int PBKDF2_ITERATIONS = 100_000;
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final byte VERSION_BYTE = 1;

    private final PiiProperties piiProperties;

    public ClientCryptoService(PiiProperties piiProperties) {
        this.piiProperties = piiProperties;
    }

    public boolean isEnabled() {
        String secret = piiProperties.getClientStorageSecret();
        return secret != null && !secret.isBlank();
    }

    public static boolean isEncrypted(String value) {
        return value != null && value.trim().startsWith(PREFIX);
    }

    public static boolean isBcryptHash(String value) {
        return value != null && value.startsWith("$2a$");
    }

    public String encrypt(String plaintext) {
        requireEnabled();
        try {
            SecretKey key = deriveAesKey(piiProperties.getClientStorageSecret());
            byte[] iv = deterministicIv(piiProperties.getClientStorageSecret(), plaintext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer packed = ByteBuffer.allocate(1 + IV_LENGTH + ciphertext.length);
            packed.put(VERSION_BYTE);
            packed.put(iv);
            packed.put(ciphertext);
            return PREFIX + Base64.getEncoder().encodeToString(packed.array());
        } catch (Exception ex) {
            throw new IllegalStateException("PII encryption failed", ex);
        }
    }

    public String decrypt(String stored) {
        requireEnabled();
        String trimmed = stored.trim();
        if (!trimmed.startsWith(PREFIX)) {
            return trimmed;
        }
        try {
            byte[] packed = Base64.getDecoder().decode(trimmed.substring(PREFIX.length()));
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(packed, 1, iv, 0, IV_LENGTH);
            byte[] ciphertext = new byte[packed.length - 1 - IV_LENGTH];
            System.arraycopy(packed, 1 + IV_LENGTH, ciphertext, 0, ciphertext.length);

            SecretKey key = deriveAesKey(piiProperties.getClientStorageSecret());
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("PII decryption failed", ex);
        }
    }

    public String sealEmail(String plainEmail) {
        return encrypt(plainEmail.trim().toLowerCase());
    }

    /**
     * Value to use in {@code app_user.email} equality lookups.
     * <ul>
     *   <li>Already {@code FLENC1:…} → use as-is (do not seal again).</li>
     *   <li>Crypto enabled → deterministic seal of normalized plaintext.</li>
     *   <li>Crypto disabled → lowercase plaintext (legacy rows).</li>
     * </ul>
     */
    public String emailForLookup(String emailFromLogin) {
        if (emailFromLogin == null || emailFromLogin.isBlank()) {
            return emailFromLogin;
        }
        String trimmed = emailFromLogin.trim();
        if (isEncrypted(trimmed)) {
            return trimmed;
        }
        if (isEnabled()) {
            return sealEmail(trimmed);
        }
        return trimmed.toLowerCase();
    }

    public String sealSecret(String plainSecret) {
        return encrypt(plainSecret);
    }

    public boolean sealedEquals(String plaintext, String storedSealed) {
        if (!isEncrypted(storedSealed)) {
            return false;
        }
        return encrypt(plaintext).equals(storedSealed);
    }

    private void requireEnabled() {
        if (!isEnabled()) {
            throw new IllegalStateException("app.pii.client-storage-secret is not configured");
        }
    }

    private static byte[] deterministicIv(String secret, String plaintext) throws Exception {
        byte[] label = "fitlife.aesgcm.iv.v1".getBytes(StandardCharsets.UTF_8);
        byte[] sb = secret.getBytes(StandardCharsets.UTF_8);
        byte[] pb = plaintext.getBytes(StandardCharsets.UTF_8);
        ByteBuffer input = ByteBuffer.allocate(label.length + sb.length + pb.length);
        input.put(label);
        input.put(sb);
        input.put(pb);
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.array());
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(hash, 0, iv, 0, IV_LENGTH);
        return iv;
    }

    private static SecretKey deriveAesKey(String secret) throws Exception {
        KeySpec spec = new PBEKeySpec(
                secret.toCharArray(),
                "fitlife.client-storage.pbkdf2.salt.v1".getBytes(StandardCharsets.UTF_8),
                PBKDF2_ITERATIONS,
                256);
        return new SecretKeySpec(
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(), "AES");
    }
}

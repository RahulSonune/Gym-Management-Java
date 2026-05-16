package com.fitlife.gym.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.gym.crypto.ClientCryptoService;
import com.fitlife.gym.exception.BusinessRuleException;
import com.fitlife.gym.web.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoginCredentialResolver {

    private final ClientCryptoService clientCrypto;
    private final ObjectMapper objectMapper;

    /** Sealed email for DB lookup; raw password for {@link HybridPasswordEncoder} matching. */
    public record ResolvedLogin(String sealedEmail, String rawPassword) {}

    public ResolvedLogin resolve(LoginRequest request) {
        if (request.getCredentials() != null && !request.getCredentials().isBlank()) {
            return resolveEnvelope(request.getCredentials().trim());
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessRuleException("Login credentials are required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessRuleException("Password is required.");
        }
        return new ResolvedLogin(clientCrypto.emailForLookup(request.getEmail()), request.getPassword());
    }

    @SuppressWarnings("unchecked")
    private ResolvedLogin resolveEnvelope(String credentials) {
        if (!clientCrypto.isEnabled()) {
            throw new BusinessRuleException("Sealed login requires app.pii.client-storage-secret on the server.");
        }
        String json;
        try {
            json = clientCrypto.decrypt(credentials);
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("Invalid login credentials.");
        }
        try {
            Map<String, String> map = objectMapper.readValue(json, Map.class);
            String email = map.get("email");
            String password = map.get("password");
            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                throw new BusinessRuleException("Invalid login credentials.");
            }
            return new ResolvedLogin(clientCrypto.emailForLookup(email), password);
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessRuleException("Invalid login credentials.");
        }
    }
}

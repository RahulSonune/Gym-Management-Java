package com.fitlife.gym.security;

import com.fitlife.gym.crypto.ClientCryptoService;
import com.fitlife.gym.domain.entity.AppUser;
import com.fitlife.gym.domain.entity.StaffBranch;
import com.fitlife.gym.domain.repository.AppUserRepository;
import com.fitlife.gym.domain.repository.StaffBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final StaffBranchRepository staffBranchRepository;
    private final ClientCryptoService clientCrypto;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = findUserForLogin(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<Long> branchIds = staffBranchRepository.findByUserId(user.getId()).stream()
                .map(StaffBranch::getBranchId)
                .collect(Collectors.toSet());

        return new GymUserPrincipal(
                user.getId(),
                user.getOrganization().getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.isActive(),
                user.getRoles(),
                branchIds);
    }

    @Transactional(readOnly = true)
    public AppUser loadAppUser(String email) {
        return findUserForLogin(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /** Lookup by login email (sealed or plaintext). Empty if no account exists. */
    @Transactional(readOnly = true)
    public java.util.Optional<AppUser> findByLoginEmail(String email) {
        return findUserForLogin(email);
    }

    private java.util.Optional<AppUser> findUserForLogin(String emailFromToken) {
        String lookupKey = clientCrypto.emailForLookup(emailFromToken);
        var found = appUserRepository.findByEmailAndDeletedAtIsNull(lookupKey);
        if (found.isPresent()) {
            return found;
        }
        if (!lookupKey.equals(emailFromToken)) {
            found = appUserRepository.findByEmailAndDeletedAtIsNull(emailFromToken);
        }
        if (found.isEmpty() && clientCrypto.isEnabled()) {
            log.warn(
                    "Login email lookup failed (crypto enabled). Check app.pii.client-storage-secret matches "
                            + "the value used when staff rows were sealed, and that app_user.email is FLENC1 for admin.");
        }
        return found;
    }
}

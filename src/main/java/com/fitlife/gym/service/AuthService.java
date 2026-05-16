package com.fitlife.gym.service;

import com.fitlife.gym.domain.entity.AppUser;
import com.fitlife.gym.domain.entity.Branch;
import com.fitlife.gym.domain.entity.StaffBranch;
import com.fitlife.gym.domain.repository.AppUserRepository;
import com.fitlife.gym.domain.repository.BranchRepository;
import com.fitlife.gym.domain.repository.StaffBranchRepository;
import com.fitlife.gym.security.CustomUserDetailsService;
import com.fitlife.gym.security.GymUserPrincipal;
import com.fitlife.gym.security.JwtTokenProvider;
import com.fitlife.gym.web.dto.AuthUserDto;
import com.fitlife.gym.web.dto.BranchSummaryDto;
import com.fitlife.gym.web.dto.LoginRequest;
import com.fitlife.gym.exception.BusinessRuleException;
import com.fitlife.gym.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEACTIVATED_MESSAGE =
            "This account has been deactivated. Contact your administrator.";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final AppUserRepository appUserRepository;
    private final StaffBranchRepository staffBranchRepository;
    private final BranchRepository branchRepository;
    private final BranchContextHelper branchContext;
    private final DtoMapper dtoMapper;
    private final LoginCredentialResolver loginCredentialResolver;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        LoginCredentialResolver.ResolvedLogin resolved = loginCredentialResolver.resolve(request);
        assertAccountEligibleForLogin(resolved.sealedEmail());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(resolved.sealedEmail(), resolved.rawPassword()));
        } catch (AuthenticationException ex) {
            if (isDeactivatedFailure(ex)) {
                throw new BusinessRuleException(DEACTIVATED_MESSAGE);
            }
            throw ex;
        }
        GymUserPrincipal principal = (GymUserPrincipal) authentication.getPrincipal();

        AppUser user = appUserRepository.findById(principal.getUserId()).orElseThrow();
        user.setLastLoginAt(Instant.now());
        appUserRepository.save(user);

        AuthUserDto authUser = buildAuthUser(user);
        return LoginResponse.builder()
                .accessToken(jwtTokenProvider.createAccessToken(principal))
                .refreshToken(jwtTokenProvider.createRefreshToken(principal))
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .user(authUser)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthUserDto currentUser() {
        GymUserPrincipal principal = branchContext.currentUser();
        AppUser user = appUserRepository.findByIdAndDeletedAtIsNull(principal.getUserId()).orElseThrow();
        return buildAuthUser(user);
    }

    private void assertAccountEligibleForLogin(String loginEmail) {
        userDetailsService
                .findByLoginEmail(loginEmail)
                .filter(user -> !user.isActive())
                .ifPresent(user -> {
                    throw new BusinessRuleException(DEACTIVATED_MESSAGE);
                });
    }

    private static boolean isDeactivatedFailure(AuthenticationException ex) {
        if (ex instanceof DisabledException) {
            return true;
        }
        Throwable cause = ex.getCause();
        return cause instanceof DisabledException;
    }

    private AuthUserDto buildAuthUser(AppUser user) {
        List<StaffBranch> assignments = staffBranchRepository.findByUserId(user.getId());
        List<Branch> branchEntities = branchRepository.findByStaffUserId(user.getId());
        List<BranchSummaryDto> branches = new ArrayList<>();
        for (Branch branch : branchEntities) {
            boolean primary = assignments.stream()
                    .anyMatch(sb -> sb.getBranchId().equals(branch.getId()) && sb.isPrimary());
            branches.add(dtoMapper.toBranchSummary(branch, primary));
        }
        return dtoMapper.toAuthUser(
                user, branches, dtoMapper.toOrganizationSummary(user.getOrganization()));
    }
}

package com.fitlife.gym.service;

import com.fitlife.gym.domain.entity.Branch;
import com.fitlife.gym.domain.repository.BranchRepository;
import com.fitlife.gym.exception.AccessDeniedException;
import com.fitlife.gym.exception.ResourceNotFoundException;
import com.fitlife.gym.security.GymUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class BranchContextHelper {

    public static final String BRANCH_HEADER = "X-Branch-Id";

    private final BranchRepository branchRepository;

    public GymUserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof GymUserPrincipal principal)) {
            throw new AccessDeniedException("Not authenticated");
        }
        return principal;
    }

    public Long resolveBranchId(Long queryBranchId) {
        GymUserPrincipal user = currentUser();
        Long branchId = queryBranchId;
        if (branchId == null) {
            branchId = readHeaderBranchId();
        }
        if (branchId == null) {
            throw new AccessDeniedException("Branch context is required");
        }
        assertBranchAccess(user, branchId);
        return branchId;
    }

    public Long resolveBranchIdOptional(Long queryBranchId) {
        GymUserPrincipal user = currentUser();
        Long branchId = queryBranchId != null ? queryBranchId : readHeaderBranchId();
        if (branchId == null) {
            return null;
        }
        assertBranchAccess(user, branchId);
        return branchId;
    }

    public void assertBranchAccess(GymUserPrincipal user, Long branchId) {
        if (user.getBranchIds().contains(branchId)) {
            return;
        }
        if (hasAdminRole(user)) {
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
            if (branch.getOrganization().getId().equals(user.getOrganizationId())) {
                return;
            }
        }
        throw new AccessDeniedException("Access denied for branch " + branchId);
    }

    private boolean hasAdminRole(GymUserPrincipal user) {
        return user.getAuthorities().stream()
                .anyMatch(a -> {
                    String role = a.getAuthority();
                    return "ROLE_SUPER_ADMIN".equals(role) || "ROLE_BRANCH_MANAGER".equals(role);
                });
    }

    private Long readHeaderBranchId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String header = request.getHeader(BRANCH_HEADER);
        if (header == null || header.isBlank()) {
            return null;
        }
        return Long.parseLong(header);
    }
}

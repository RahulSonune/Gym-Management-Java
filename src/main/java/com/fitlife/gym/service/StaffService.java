package com.fitlife.gym.service;

import com.fitlife.gym.domain.entity.AppUser;
import com.fitlife.gym.domain.entity.Branch;
import com.fitlife.gym.domain.entity.Organization;
import com.fitlife.gym.domain.entity.StaffBranch;
import com.fitlife.gym.domain.repository.AppUserRepository;
import com.fitlife.gym.domain.repository.BranchRepository;
import com.fitlife.gym.domain.repository.OrganizationRepository;
import com.fitlife.gym.domain.repository.StaffBranchRepository;
import com.fitlife.gym.exception.AccessDeniedException;
import com.fitlife.gym.exception.BusinessRuleException;
import com.fitlife.gym.crypto.ClientCryptoService;
import com.fitlife.gym.exception.ResourceNotFoundException;
import com.fitlife.gym.security.GymUserPrincipal;
import com.fitlife.gym.web.dto.CreateStaffRequest;
import com.fitlife.gym.web.dto.StaffUserDto;
import com.fitlife.gym.web.dto.UpdateStaffRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private static final Set<String> ALL_ROLES = Set.of(
            "SUPER_ADMIN",
            "BRANCH_MANAGER",
            "RECEPTIONIST",
            "TRAINER",
            "ACCOUNTANT");

    private final AppUserRepository appUserRepository;
    private final OrganizationRepository organizationRepository;
    private final BranchRepository branchRepository;
    private final StaffBranchRepository staffBranchRepository;
    private final BranchContextHelper branchContext;
    private final PasswordEncoder passwordEncoder;
    private final ClientCryptoService clientCrypto;

    @Transactional(readOnly = true)
    public List<StaffUserDto> list() {
        GymUserPrincipal actor = branchContext.currentUser();
        assertCanManageStaff(actor);

        return appUserRepository.findByOrganizationIdAndDeletedAtIsNullOrderByFullNameAsc(
                        actor.getOrganizationId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public StaffUserDto create(CreateStaffRequest request) {
        GymUserPrincipal actor = branchContext.currentUser();
        assertCanManageStaff(actor);
        validateCreateRequest(request);

        String email = normalizeStoredEmail(request.getEmail());
        Set<String> roles = normalizeRoles(request.getRoles());
        validateRolesForActor(actor, roles);

        if (appUserRepository.existsByOrganizationIdAndEmailAndDeletedAtIsNull(
                actor.getOrganizationId(), email)) {
            throw new BusinessRuleException("A staff member with this email already exists.");
        }

        Organization organization = organizationRepository
                .findById(actor.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        List<Long> branchIds = request.getBranchIds().stream().distinct().toList();
        Map<Long, Branch> branches = resolveBranches(actor, branchIds);

        Long primaryBranchId = request.getPrimaryBranchId() != null
                ? request.getPrimaryBranchId()
                : branchIds.get(0);
        if (!branches.containsKey(primaryBranchId)) {
            throw new BusinessRuleException("Primary branch must be one of the assigned branches.");
        }

        AppUser user = AppUser.builder()
                .organization(organization)
                .email(email)
                .phone(normalizeStoredOptionalPii(request.getPhone()))
                .passwordHash(normalizeStoredPassword(request.getPassword()))
                .fullName(normalizeStoredRequiredPii(request.getFullName()))
                .isActive(true)
                .roles(new HashSet<>(roles))
                .build();

        AppUser saved = appUserRepository.save(user);

        for (Long branchId : branchIds) {
            staffBranchRepository.save(StaffBranch.builder()
                    .userId(saved.getId())
                    .branchId(branchId)
                    .isPrimary(branchId.equals(primaryBranchId))
                    .build());
        }

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public StaffUserDto getById(Long id) {
        GymUserPrincipal actor = branchContext.currentUser();
        assertCanManageStaff(actor);
        AppUser user = loadStaffUserForActor(actor, id);
        return toDto(user);
    }

    @Transactional
    public StaffUserDto update(Long id, UpdateStaffRequest request) {
        GymUserPrincipal actor = branchContext.currentUser();
        assertCanManageStaff(actor);
        validateUpdateRequest(request);

        AppUser user = loadStaffUserForActor(actor, id);
        assertActorCanModifyTarget(actor, user);
        if (actor.getUserId().equals(user.getId())
                && request.getIsActive() != null
                && !request.getIsActive()) {
            throw new BusinessRuleException("You cannot deactivate your own account.");
        }

        String email = normalizeStoredEmail(request.getEmail());
        Set<String> roles = normalizeRoles(request.getRoles());
        validateRolesForActor(actor, roles);

        if (appUserRepository.existsByOrganizationIdAndEmailAndDeletedAtIsNullAndIdNot(
                actor.getOrganizationId(), email, user.getId())) {
            throw new BusinessRuleException("A staff member with this email already exists.");
        }

        List<Long> branchIds = request.getBranchIds().stream().distinct().toList();
        Map<Long, Branch> branches = resolveBranches(actor, branchIds);

        Long primaryBranchId = request.getPrimaryBranchId() != null
                ? request.getPrimaryBranchId()
                : branchIds.get(0);
        if (!branches.containsKey(primaryBranchId)) {
            throw new BusinessRuleException("Primary branch must be one of the assigned branches.");
        }

        user.setFullName(normalizeStoredRequiredPii(request.getFullName()));
        user.setEmail(email);
        user.setPhone(normalizeStoredOptionalPii(request.getPhone()));
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(normalizeStoredPassword(request.getPassword()));
        }
        user.setRoles(new HashSet<>(roles));
        if (request.getIsActive() != null) {
            user.setActive(Boolean.TRUE.equals(request.getIsActive()));
        }

        AppUser saved = appUserRepository.save(user);

        staffBranchRepository.deleteByUserId(saved.getId());
        for (Long branchId : branchIds) {
            staffBranchRepository.save(StaffBranch.builder()
                    .userId(saved.getId())
                    .branchId(branchId)
                    .isPrimary(branchId.equals(primaryBranchId))
                    .build());
        }

        return toDto(saved);
    }

    private AppUser loadStaffUserForActor(GymUserPrincipal actor, Long id) {
        AppUser user = appUserRepository
                .findByIdAndOrganizationIdAndDeletedAtIsNull(id, actor.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));
        if (!isSuperAdmin(actor) && user.getRoles().contains("SUPER_ADMIN")) {
            throw new AccessDeniedException("Only a super admin can view or edit super admin accounts.");
        }
        return user;
    }

    private void assertActorCanModifyTarget(GymUserPrincipal actor, AppUser target) {
        if (!isSuperAdmin(actor) && target.getRoles().contains("SUPER_ADMIN")) {
            throw new AccessDeniedException("Only a super admin can edit super admin accounts.");
        }
    }

    private StaffUserDto toDto(AppUser user) {
        List<StaffBranch> links = staffBranchRepository.findByUserId(user.getId());
        List<Long> branchIds = links.stream().map(StaffBranch::getBranchId).toList();
        List<String> branchNames = branchIds.stream()
                .map(branchRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Branch::getName)
                .toList();
        Long primaryBranchId = links.stream()
                .filter(StaffBranch::isPrimary)
                .map(StaffBranch::getBranchId)
                .findFirst()
                .orElse(branchIds.isEmpty() ? null : branchIds.get(0));

        return StaffUserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles().stream().sorted().toList())
                .isActive(user.isActive())
                .branchIds(branchIds)
                .branchNames(branchNames)
                .primaryBranchId(primaryBranchId)
                .build();
    }

    private void assertCanManageStaff(GymUserPrincipal actor) {
        if (isSuperAdmin(actor) || isBranchManager(actor)) {
            return;
        }
        throw new AccessDeniedException("You do not have permission to manage staff.");
    }

    private void validateRolesForActor(GymUserPrincipal actor, Set<String> roles) {
        if (!isSuperAdmin(actor) && roles.contains("SUPER_ADMIN")) {
            throw new AccessDeniedException("Only a super admin can assign the Super Admin role.");
        }
    }

    private Set<String> normalizeRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new BusinessRuleException("Select at least one role.");
        }
        Set<String> normalized = roles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String role : normalized) {
            if (!ALL_ROLES.contains(role)) {
                throw new BusinessRuleException("Invalid role: " + role);
            }
        }
        return normalized;
    }

    private Map<Long, Branch> resolveBranches(GymUserPrincipal actor, List<Long> branchIds) {
        Map<Long, Branch> result = new LinkedHashMap<>();
        for (Long branchId : branchIds) {
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new BusinessRuleException("Branch not found: " + branchId));
            if (!branch.getOrganization().getId().equals(actor.getOrganizationId())) {
                throw new BusinessRuleException("Branch does not belong to your organization.");
            }
            if (!isSuperAdmin(actor) && !actor.getBranchIds().contains(branchId)) {
                throw new AccessDeniedException("You can only assign branches you have access to.");
            }
            result.put(branchId, branch);
        }
        return result;
    }

    private boolean isSuperAdmin(GymUserPrincipal actor) {
        return actor.getAuthorities().stream().anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }

    private boolean isBranchManager(GymUserPrincipal actor) {
        return actor.getAuthorities().stream().anyMatch(a -> "ROLE_BRANCH_MANAGER".equals(a.getAuthority()));
    }

    private void validateCreateRequest(CreateStaffRequest request) {
        validateEmail(request.getEmail());
        validatePasswordRequired(request.getPassword());
    }

    private void validateUpdateRequest(UpdateStaffRequest request) {
        validateEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            validatePasswordRequired(request.getPassword());
        }
    }

    private void validateEmail(String rawEmail) {
        String email = rawEmail.trim();
        if (!ClientCryptoService.isEncrypted(email)
                && !email.matches("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            throw new BusinessRuleException("Enter a valid email address.");
        }
    }

    private void validatePasswordRequired(String rawPassword) {
        if (!ClientCryptoService.isEncrypted(rawPassword) && rawPassword.length() < 6) {
            throw new BusinessRuleException("Password must be at least 6 characters.");
        }
    }

    private String normalizeStoredEmail(String raw) {
        String email = raw.trim();
        if (ClientCryptoService.isEncrypted(email)) {
            return email;
        }
        if (clientCrypto.isEnabled()) {
            try {
                return clientCrypto.sealEmail(email);
            } catch (RuntimeException ex) {
                throw new BusinessRuleException("Could not encrypt email. Check server PII configuration.");
            }
        }
        return email.toLowerCase();
    }

    private String normalizeStoredPassword(String raw) {
        if (ClientCryptoService.isEncrypted(raw)) {
            return raw;
        }
        try {
            return passwordEncoder.encode(raw);
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("Could not store password. Check server PII configuration.");
        }
    }

    private String normalizeStoredRequiredPii(String raw) {
        String value = raw.trim();
        if (ClientCryptoService.isEncrypted(value)) {
            return value;
        }
        if (clientCrypto.isEnabled()) {
            try {
                return clientCrypto.encrypt(value);
            } catch (RuntimeException ex) {
                throw new BusinessRuleException("Could not encrypt staff data. Check server PII configuration.");
            }
        }
        return value;
    }

    private String normalizeStoredOptionalPii(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return normalizeStoredRequiredPii(raw);
    }
}

package com.fitlife.gym.service;

import com.fitlife.gym.domain.entity.Branch;
import com.fitlife.gym.domain.repository.BranchRepository;
import com.fitlife.gym.security.GymUserPrincipal;
import com.fitlife.gym.web.dto.BranchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final BranchContextHelper branchContext;
    private final DtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<BranchDto> listForCurrentUser() {
        GymUserPrincipal user = branchContext.currentUser();
        List<Branch> branches;
        if (user.getAuthorities().stream().anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()))) {
            branches = branchRepository.findActiveByOrganizationId(user.getOrganizationId());
        } else {
            branches = branchRepository.findByStaffUserId(user.getUserId());
        }
        return branches.stream()
                .sorted(Comparator.comparing(Branch::isDefault).reversed().thenComparing(Branch::getName))
                .map(dtoMapper::toBranchDto)
                .toList();
    }
}

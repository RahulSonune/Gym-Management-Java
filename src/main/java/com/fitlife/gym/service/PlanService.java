package com.fitlife.gym.service;

import com.fitlife.gym.domain.entity.MembershipPlan;
import com.fitlife.gym.domain.repository.MembershipPlanRepository;
import com.fitlife.gym.security.GymUserPrincipal;
import com.fitlife.gym.web.dto.MembershipPlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final MembershipPlanRepository planRepository;
    private final BranchContextHelper branchContext;
    private final DtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<MembershipPlanDto> list(boolean activeOnly) {
        GymUserPrincipal user = branchContext.currentUser();
        return planRepository.findByOrganization(user.getOrganizationId(), activeOnly).stream()
                .map(dtoMapper::toPlanDto)
                .toList();
    }
}

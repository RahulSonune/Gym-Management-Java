package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    @Query("""
            SELECT p FROM MembershipPlan p
            WHERE p.organization.id = :orgId
              AND (:activeOnly = false OR p.isActive = true)
            ORDER BY p.sortOrder, p.name
            """)
    List<MembershipPlan> findByOrganization(
            @Param("orgId") Long organizationId,
            @Param("activeOnly") boolean activeOnly);
}

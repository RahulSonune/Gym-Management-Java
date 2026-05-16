package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Query("""
            SELECT b FROM Branch b
            WHERE b.organization.id = :orgId
              AND b.deletedAt IS NULL
              AND b.isActive = true
            ORDER BY b.isDefault DESC, b.name
            """)
    List<Branch> findActiveByOrganizationId(@Param("orgId") Long organizationId);

    @Query("""
            SELECT b FROM Branch b
            JOIN StaffBranch sb ON sb.branchId = b.id
            WHERE sb.userId = :userId
              AND b.deletedAt IS NULL
              AND b.isActive = true
            ORDER BY sb.isPrimary DESC, b.name
            """)
    List<Branch> findByStaffUserId(@Param("userId") Long userId);
}

package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("""
            SELECT i FROM Invoice i
            JOIN FETCH i.member
            WHERE i.organization.id = :orgId
              AND (:branchId IS NULL OR i.branch.id = :branchId)
            ORDER BY i.issueDate DESC, i.id DESC
            """)
    List<Invoice> findByOrganizationAndBranch(
            @Param("orgId") Long organizationId,
            @Param("branchId") Long branchId);

    long countByBranchIdAndStatus(Long branchId, String status);

    @Query("""
            SELECT COALESCE(SUM(i.totalMinor - i.amountPaidMinor), 0)
            FROM Invoice i
            JOIN Member m ON m.id = i.member.id
            WHERE m.id = :memberId
              AND i.status IN ('ISSUED', 'PARTIAL', 'OVERDUE')
            """)
    long sumOutstandingForMember(@Param("memberId") Long memberId);
}

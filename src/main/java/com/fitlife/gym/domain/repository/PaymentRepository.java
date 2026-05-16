package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            SELECT p FROM Payment p
            JOIN FETCH p.member
            WHERE p.organization.id = :orgId
              AND (:branchId IS NULL OR p.branch.id = :branchId)
            ORDER BY p.paidAt DESC, p.id DESC
            """)
    List<Payment> findByOrganizationAndBranch(
            @Param("orgId") Long organizationId,
            @Param("branchId") Long branchId);

    @Query("""
            SELECT COALESCE(SUM(p.amountMinor), 0)
            FROM Payment p
            WHERE p.branch.id = :branchId
              AND p.status = 'SUCCESS'
              AND p.paidAt >= :start
              AND p.paidAt < :end
            """)
    long sumRevenueBetween(
            @Param("branchId") Long branchId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query("SELECT COALESCE(MAX(p.id), 0) FROM Payment p WHERE p.organization.id = :orgId")
    long maxIdForOrg(@Param("orgId") Long organizationId);
}

package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("""
            SELECT s FROM Subscription s
            JOIN FETCH s.plan
            WHERE s.member.id = :memberId
              AND s.status = 'ACTIVE'
              AND s.endDate >= :today
            ORDER BY s.endDate DESC
            """)
    Optional<Subscription> findActiveForMember(
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today);

    @Query("""
            SELECT s FROM Subscription s
            JOIN FETCH s.plan p
            JOIN FETCH s.member m
            WHERE s.branch.id = :branchId
              AND s.status = 'ACTIVE'
              AND s.endDate BETWEEN :from AND :to
            ORDER BY s.endDate, m.firstName
            """)
    List<Subscription> findExpiringInBranch(
            @Param("branchId") Long branchId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT COUNT(s) FROM Subscription s
            WHERE s.branch.id = :branchId
              AND s.status = 'ACTIVE'
              AND s.endDate BETWEEN :from AND :to
            """)
    long countExpiringInBranch(
            @Param("branchId") Long branchId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}

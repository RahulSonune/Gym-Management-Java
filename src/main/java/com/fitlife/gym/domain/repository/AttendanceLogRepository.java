package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    @Query("""
            SELECT a FROM AttendanceLog a
            JOIN FETCH a.member m
            WHERE a.branch.id = :branchId
              AND a.checkInAt >= :start
              AND a.checkInAt < :end
              AND a.deniedReason IS NULL
            ORDER BY a.checkInAt DESC
            """)
    List<AttendanceLog> findByBranchAndDateRange(
            @Param("branchId") Long branchId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query("""
            SELECT a FROM AttendanceLog a
            JOIN FETCH a.member m
            WHERE a.branch.id = :branchId
              AND a.checkInAt >= :start
              AND a.checkInAt < :end
              AND a.checkOutAt IS NULL
              AND a.deniedReason IS NULL
            ORDER BY a.checkInAt DESC
            """)
    List<AttendanceLog> findCurrentlyInside(
            @Param("branchId") Long branchId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    long countByBranchIdAndCheckInAtBetweenAndDeniedReasonIsNull(
            Long branchId, Instant start, Instant end);

    long countByMemberIdAndDeniedReasonIsNull(Long memberId);

    @Query("""
            SELECT MAX(a.checkInAt) FROM AttendanceLog a
            WHERE a.member.id = :memberId AND a.deniedReason IS NULL
            """)
    Optional<Instant> findLastVisitAt(@Param("memberId") Long memberId);

    @Query("""
            SELECT a FROM AttendanceLog a
            WHERE a.member.id = :memberId
              AND a.branch.id = :branchId
              AND a.checkInAt >= :start
              AND a.checkOutAt IS NULL
              AND a.deniedReason IS NULL
            """)
    Optional<AttendanceLog> findOpenCheckIn(
            @Param("memberId") Long memberId,
            @Param("branchId") Long branchId,
            @Param("start") Instant start);
}

package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
            SELECT m FROM Member m
            JOIN FETCH m.branch b
            WHERE m.deletedAt IS NULL
              AND m.organization.id = :orgId
              AND (:branchId IS NULL OR m.branch.id = :branchId)
              AND (:status IS NULL OR m.status = :status)
              AND (
                :search IS NULL OR :search = '' OR
                LOWER(m.memberCode) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(m.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(m.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                m.phone LIKE CONCAT('%', :search, '%') OR
                LOWER(COALESCE(m.email, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Member> search(
            @Param("orgId") Long organizationId,
            @Param("branchId") Long branchId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
            SELECT m FROM Member m
            JOIN FETCH m.branch
            WHERE m.deletedAt IS NULL
              AND m.organization.id = :orgId
              AND m.branch.id = :branchId
              AND (
                LOWER(m.memberCode) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(m.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(COALESCE(m.lastName, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
                m.phone LIKE CONCAT('%', :q, '%')
              )
            ORDER BY m.firstName, m.lastName
            """)
    List<Member> lookup(
            @Param("orgId") Long organizationId,
            @Param("branchId") Long branchId,
            @Param("q") String query);

    @Query("""
            SELECT m FROM Member m
            JOIN FETCH m.branch
            WHERE m.id = :id AND m.deletedAt IS NULL AND m.organization.id = :orgId
            """)
    Optional<Member> findByIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long organizationId);

    /**
     * Same stored {@code phone} value = same person when client encryption is deterministic per plaintext.
     */
    @Query("""
            SELECT COUNT(m) FROM Member m
            WHERE m.organization.id = :orgId
              AND m.deletedAt IS NULL
              AND m.phone = :phone
              AND (:excludeId IS NULL OR m.id <> :excludeId)
            """)
    long countActiveByOrganizationIdAndPhoneExcluding(
            @Param("orgId") Long organizationId,
            @Param("excludeId") Long excludeId,
            @Param("phone") String phone);

    long countByBranchIdAndStatusAndDeletedAtIsNull(Long branchId, String status);

    @Query("""
            SELECT COUNT(m) FROM Member m
            WHERE m.branch.id = :branchId
              AND m.deletedAt IS NULL
              AND m.joinedAt = :date
            """)
    long countNewMembersOnDate(@Param("branchId") Long branchId, @Param("date") LocalDate date);
}

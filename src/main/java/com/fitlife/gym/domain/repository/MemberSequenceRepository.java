package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.MemberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberSequenceRepository extends JpaRepository<MemberSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM MemberSequence s WHERE s.branchId = :branchId")
    Optional<MemberSequence> findByBranchIdForUpdate(@Param("branchId") Long branchId);
}

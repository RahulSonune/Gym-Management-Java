package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.StaffBranch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffBranchRepository extends JpaRepository<StaffBranch, StaffBranch.StaffBranchId> {

    List<StaffBranch> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}

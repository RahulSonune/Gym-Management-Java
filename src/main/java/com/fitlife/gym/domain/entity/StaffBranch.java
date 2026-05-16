package com.fitlife.gym.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "staff_branch")
@IdClass(StaffBranch.StaffBranchId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffBranch {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "branch_id")
    private Long branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", insertable = false, updatable = false)
    private Branch branch;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StaffBranchId implements Serializable {
        private Long userId;
        private Long branchId;
    }
}

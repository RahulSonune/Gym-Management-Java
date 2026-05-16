package com.fitlife.gym.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_sequence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSequence {

    @Id
    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "seq_value", nullable = false)
    private long seqValue;
}

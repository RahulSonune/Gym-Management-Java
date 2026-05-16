package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class MemberDto {
    Long id;
    String memberCode;
    String firstName;
    String lastName;
    String fullName;
    String phone;
    String email;
    String gender;
    String dateOfBirth;
    String status;
    Long branchId;
    BranchSummaryDto branch;
    String emergencyContactName;
    String emergencyContactPhone;
    String joinedAt;
    String source;
    Instant createdAt;
}

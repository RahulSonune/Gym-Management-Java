package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExpiringMemberDto {
    Long memberId;
    String memberCode;
    String fullName;
    String phone;
    String planName;
    String endDate;
    long daysRemaining;
}

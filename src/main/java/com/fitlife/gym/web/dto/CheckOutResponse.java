package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CheckOutResponse {
    boolean allowed;
    Long attendanceId;
    Instant checkInAt;
    Instant checkOutAt;
    CheckInResponse.MemberBriefDto member;
    String deniedReason;
    String message;
}

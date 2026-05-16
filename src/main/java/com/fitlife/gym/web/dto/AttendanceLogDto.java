package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AttendanceLogDto {
    Long id;
    Long memberId;
    String memberName;
    String memberCode;
    Long branchId;
    Instant checkInAt;
    Instant checkOutAt;
    String method;
}

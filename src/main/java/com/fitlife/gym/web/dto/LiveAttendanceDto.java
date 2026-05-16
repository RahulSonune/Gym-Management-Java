package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class LiveAttendanceDto {
    Long attendanceId;
    Long memberId;
    String fullName;
    String memberCode;
    Instant checkInAt;
    String planName;
}

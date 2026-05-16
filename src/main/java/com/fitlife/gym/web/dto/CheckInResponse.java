package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CheckInResponse {
    boolean allowed;
    Long attendanceId;
    Instant checkInAt;
    MemberBriefDto member;
    SubscriptionBriefDto subscription;
    String deniedReason;
    String message;

    @Value
    @Builder
    public static class MemberBriefDto {
        Long id;
        String fullName;
        String memberCode;
    }

    @Value
    @Builder
    public static class SubscriptionBriefDto {
        String planName;
        String endDate;
    }
}

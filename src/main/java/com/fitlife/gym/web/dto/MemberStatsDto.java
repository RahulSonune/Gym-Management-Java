package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class MemberStatsDto {
    long totalVisits;
    Instant lastVisitAt;
    long outstandingAmountMinor;
}

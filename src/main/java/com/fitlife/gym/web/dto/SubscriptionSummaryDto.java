package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubscriptionSummaryDto {
    Long id;
    String planName;
    String status;
    String startDate;
    String endDate;
    Long daysRemaining;
}

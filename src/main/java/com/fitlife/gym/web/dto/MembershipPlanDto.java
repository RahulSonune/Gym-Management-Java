package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class MembershipPlanDto {
    Long id;
    String code;
    String name;
    String description;
    int durationDays;
    long priceAmountMinor;
    String currencyCode;
    BigDecimal taxPercent;
    int maxFreezeDays;
    boolean allowsPt;
    boolean allowsClasses;
    Long branchId;
    boolean isActive;
}

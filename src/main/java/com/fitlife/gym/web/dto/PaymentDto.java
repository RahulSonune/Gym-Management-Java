package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class PaymentDto {
    Long id;
    String paymentNumber;
    Long memberId;
    String memberName;
    Long branchId;
    long amountMinor;
    String currencyCode;
    String method;
    String status;
    Instant paidAt;
    String notes;
}

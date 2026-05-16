package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InvoiceDto {
    Long id;
    String invoiceNumber;
    Long memberId;
    String memberName;
    Long branchId;
    String status;
    String issueDate;
    String dueDate;
    long subtotalMinor;
    long taxMinor;
    long discountMinor;
    long totalMinor;
    long amountPaidMinor;
    String currencyCode;
}

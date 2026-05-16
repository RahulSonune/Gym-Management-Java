package com.fitlife.gym.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RecordPaymentRequest {

    @NotNull
    private Long memberId;

    @NotNull
    private Long branchId;

    @Positive
    private long amountMinor;

    @NotBlank
    private String method;

    private Long invoiceId;
    private String notes;

    @NotBlank
    private String idempotencyKey;
}

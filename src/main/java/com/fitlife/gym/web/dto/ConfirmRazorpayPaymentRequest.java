package com.fitlife.gym.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ConfirmRazorpayPaymentRequest {

    @NotNull
    private Long memberId;

    @NotNull
    private Long branchId;

    @Positive
    private long amountMinor;

    private String notes;

    @NotBlank
    private String idempotencyKey;

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpaySignature;
}

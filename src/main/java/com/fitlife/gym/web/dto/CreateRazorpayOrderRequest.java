package com.fitlife.gym.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateRazorpayOrderRequest {

    @NotNull
    private Long memberId;

    @NotNull
    private Long branchId;

    @Positive
    private long amountMinor;

    /** Optional note (shown on gym records, not sent to Razorpay). */
    private String notes;
}

package com.fitlife.gym.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * JSON for {@code GET /api/v1/payments/razorpay/config} — matches frontend
 * {@code RazorpayConfig} ({@code billing.model.ts}: {@code enabled}, {@code keyId}).
 */
@Value
@Builder
public class RazorpayConfigDto {
    /** Whether Razorpay Checkout can be used (keys present and enabled in config). */
    @JsonProperty("enabled")
    boolean enabled;

    /** Publishable key id ({@code rzp_test_...}) for Razorpay.js; {@code null} when disabled. */
    @JsonProperty("keyId")
    String keyId;
}

package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateRazorpayOrderResponse {
    /** Publishable key for Razorpay Checkout on the client. */
    String keyId;
    /** Razorpay order id (e.g. order_xx). */
    String orderId;
    /** Amount in paise (integer). */
    long amount;
    String currency;
}

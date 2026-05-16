package com.fitlife.gym.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for Razorpay. Registered via {@link org.springframework.boot.context.properties.EnableConfigurationProperties}
 * on the Spring Boot application class — do not add {@code @Component}.
 */
@Data
@ConfigurationProperties(prefix = "app.razorpay")
public class RazorpayProperties {

    /**
     * When false, Razorpay endpoints return 503 and manual payments still work.
     */
    private boolean enabled = false;

    /** Publishable key (safe to expose to frontend — also returned by order API). */
    private String keyId = "";

    /** Secret key — server only; never commit real values. */
    private String keySecret = "";
}

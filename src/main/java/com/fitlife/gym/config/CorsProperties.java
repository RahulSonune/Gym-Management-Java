package com.fitlife.gym.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /** Exact origins (production). */
    private List<String> allowedOrigins = List.of();

    /** Dev-friendly patterns, e.g. http://localhost:* */
    private List<String> allowedOriginPatterns = List.of(
            "http://localhost:*",
            "http://127.0.0.1:*");
}

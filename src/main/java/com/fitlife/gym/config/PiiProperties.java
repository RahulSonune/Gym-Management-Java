package com.fitlife.gym.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.pii")
public class PiiProperties {

    private String storageMode = "client-encrypted-opaque";

    /** Must match Angular {@code environment.clientStorageSecret}. */
    private String clientStorageSecret = "";
}

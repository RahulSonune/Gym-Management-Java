package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ApiErrorResponse {
    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
    String requestId;
}

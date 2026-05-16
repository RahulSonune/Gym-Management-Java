package com.fitlife.gym.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckInRequest {

    @NotNull
    private Long memberId;

    @NotNull
    private Long branchId;

    @NotBlank
    private String method;

    private String deviceId;
}

package com.fitlife.gym.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateStaffRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 512)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Size(max = 512)
    private String email;

    @Size(max = 512)
    private String phone;

    /** Omit or leave blank to keep the current password. */
    @Size(max = 512)
    private String password;

    @NotEmpty(message = "Select at least one role")
    private List<String> roles;

    @NotEmpty(message = "Select at least one branch")
    private List<Long> branchIds;

    private Long primaryBranchId;

    @JsonProperty("isActive")
    @JsonAlias("active")
    private Boolean isActive;
}
